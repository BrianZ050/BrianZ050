import socket
import struct
import random
import json

# Example query spec as JSON
dns_query_spec = {
    "id": random.randint(0, 65535),
    "qr": 0,      # query
    "opcode": 0,  # standard query
    "rd": 0,      # NO recursion desired (iterative resolver)
    "questions": [
        {
            "qname": "ilab1.cs.rutgers.edu",
            "qtype": 1,   # A record
            "qclass": 1   # IN
        }
    ]
}


def build_query(query_spec):
    # Header fields
    ID = query_spec["id"]
    QR = query_spec["qr"] << 15
    OPCODE = query_spec["opcode"] << 11
    AA, TC = 0, 0
    RD = query_spec["rd"] << 8
    RA, Z, RCODE = 0, 0, 0
    flags = QR | OPCODE | AA | TC | RD | RA | Z | RCODE

    QDCOUNT = len(query_spec["questions"])
    ANCOUNT, NSCOUNT, ARCOUNT = 0, 0, 0

    header = struct.pack("!HHHHHH", ID, flags, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT)

    # Question section
    question_bytes = b""
    for q in query_spec["questions"]:
        labels = q["qname"].split(".")
        for label in labels:
            question_bytes += struct.pack("B", len(label)) + label.encode()
        question_bytes += b"\x00"  # end of qname
        question_bytes += struct.pack("!HH", q["qtype"], q["qclass"])

    return header + question_bytes


def parse_name(data, offset):
    labels = []
    jumped = False
    original_offset = offset

    while True:
        length = data[offset]
        if length == 0:
            offset += 1
            break
        # pointer
        if (length & 0xC0) == 0xC0:
            if not jumped:
                original_offset = offset + 2
            pointer = struct.unpack("!H", data[offset:offset+2])[0]
            offset = pointer & 0x3FFF
            jumped = True
            continue
        labels.append(data[offset+1:offset+1+length].decode())
        offset += length + 1

    if not jumped:
        return ".".join(labels), offset
    else:
        return ".".join(labels), original_offset

#your parse_rr from part2
def parse_rr(data, offset):
    """Parse a single resource record and return record + new offset."""
    name, offset = parse_name(data, offset)
    atype, aclass, ttl, rdlength = struct.unpack("!HHIH", data[offset:offset+10])
    offset += 10
    rdata = data[offset:offset+rdlength]
    offset += rdlength
    
    record = {
        "name": name,
        "type": atype,
        "class": aclass,
        "ttl": ttl,
        "rdlength": rdlength
    }
    
    # Extract data based on record type
    if atype == 1 and rdlength == 4:  # A record (IPv4)
        ip_addr = ".".join(str(b) for b in rdata)
        record["ip"] = ip_addr
    elif atype == 28 and rdlength == 16:  # AAAA record (IPv6)
        ip_addr = ":".join("{:02x}{:02x}".format(rdata[i], rdata[i+1]) for i in range(0, 16, 2))
        record["ip"] = ip_addr
    elif atype == 2:  # NS record (Name Server)
        try:
            # For NS records, name might use pointers to the original packet
            # So we parse from the full data packet
            ns_name, _ = parse_name(data, offset - rdlength)
            record["nsname"] = ns_name
        except:
            # If parsing fails, try to decode as a simple name
            try:
                ns_name, _ = parse_name(rdata, 0)
                record["nsname"] = ns_name
            except:
                record["nsname"] = str(rdata)
    
    return record, offset

def parse_response(data):
    response = {}
    (ID, flags, QDCOUNT, ANCOUNT, NSCOUNT, ARCOUNT) = struct.unpack("!HHHHHH", data[:12])

    response["id"] = ID
    response["qr"] = (flags >> 15) & 1
    response["opcode"] = (flags >> 11) & 0xF
    response["aa"] = (flags >> 10) & 1
    response["tc"] = (flags >> 9) & 1
    response["rd"] = (flags >> 8) & 1
    response["ra"] = (flags >> 7) & 1
    response["rcode"] = flags & 0xF
    response["qdcount"] = QDCOUNT
    response["ancount"] = ANCOUNT
    response["nscount"] = NSCOUNT
    response["arcount"] = ARCOUNT

    offset = 12
    # Skip questions
    for _ in range(QDCOUNT):
        while data[offset] != 0:
            offset += data[offset] + 1
        offset += 1
        offset += 4  # qtype + qclass

    # Parse Answer RRs
    answers = []
    for _ in range(ANCOUNT):
        rr, offset = parse_rr(data, offset)
        answers.append(rr)
    
    # Parse Authority RRs (NS)
    authorities = []
    for _ in range(NSCOUNT):
        rr, offset = parse_rr(data, offset)
        authorities.append(rr)
    
    # Parse Additional RRs (A, AAAA, etc.)
    additionals = []
    for _ in range(ARCOUNT):
        rr, offset = parse_rr(data, offset)
        additionals.append(rr)
    
    response["answers"] = answers
    response["authorities"] = authorities
    response["additionals"] = additionals

    return response



def dns_query(query_spec, server=("1.1.1.1", 53)):
    query = build_query(query_spec)
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.settimeout(5)
    sock.sendto(query, server)
    data, _ = sock.recvfrom(512)
    sock.close()
    return parse_response(data)



def iterative_resolve(query_spec):
    """
    Iterative resolver that starts at root servers and follows referrals.
    Returns the answer if found, or error if no glue records available.
    """
    servers = ["198.41.0.4"]  # Start with first root server IP
    query_history = []
    
    while servers: 
        server_ip = servers.pop(0)
        
        # Modify query_spec to have rd=0 (no recursion for iterative)
        iterative_spec = query_spec.copy()
        iterative_spec["rd"] = 0
        
        # Send query to current server
        try:
            response = dns_query(iterative_spec, server=(server_ip, 53))
        except Exception as e:
            print(f"Error querying {server_ip}: {e}")
            continue
        
        query_history.append({
            "server": server_ip,
            "response": response
        })
        
        # Check if we have an answer (ANCOUNT > 0 and answer section has A/AAAA records)
        if response.get("ancount", 0) > 0:
            answers = response.get("answers", [])
            # Look for A or AAAA records in answers
            for answer in answers:
                if answer.get("type") in [1, 28]:  # A or AAAA
                    return {
                        "success": True,
                        "answer": answer,
                        "response": response,
                        "query_history": query_history
                    }
        
        # No answer yet, look for referral (NS records in authority + glue in additional)
        authorities = response.get("authorities", [])
        additionals = response.get("additionals", [])
        
        # Extract NS records and look for glue records (A records in additional)
        ns_found = False
        for authority in authorities:
            if authority.get("type") == 2:  # NS record
                ns_name = authority.get("nsname")
                ns_found = True
                
                # Look for glue record (A record) for this NS
                for additional in additionals:
                    if additional.get("name") == ns_name and additional.get("type") == 1:
                        # Found glue record, add to servers to query
                        glue_ip = additional.get("ip")
                        if glue_ip:
                            servers.append(glue_ip)
        
        # If we found NS records but no glue records, return error
        if ns_found and not servers:
            return {
                "success": False,
                "error": "No glue records found",
                "query_history": query_history
            }
        
        # If no NS records found and no answer, we're stuck
        if not ns_found:
            return {
                "success": False,
                "error": "No referral found",
                "query_history": query_history
            }
    
    return {
        "success": False,
        "error": "No servers to query",
        "query_history": query_history
    }




if __name__ == "__main__":
    # Read questions from Input.json
    with open("Input.json", "r") as f:
        query_json = json.load(f)
    
    results = []
    
    # Process each question
    for q in query_json:
        # Create dns_query_spec for iterative resolution
        dns_query_spec = {
            "id": 1337,
            "qr": 0,      # query
            "opcode": 0,  # standard query
            "rd": 0,      # NO recursion desired for iterative
            "questions": [
                {
                    "qname": q["qname"],
                    "qtype": q["qtype"],
                    "qclass": 1   # IN
                }
            ]
        }
        
        # Perform iterative resolution
        result = iterative_resolve(dns_query_spec)
        
        results.append({
            "query": q,
            "result": result
        })
        
        # Print for debugging
        print(json.dumps(results[-1], indent=2, default=str))
    
    # Write results to output
    with open("output_partc.json", "w") as f:
        json.dump(results, f, indent=2, default=str)
    
