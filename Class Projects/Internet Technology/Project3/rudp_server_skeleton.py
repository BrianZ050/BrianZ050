#!/usr/bin/env python3
"""
rudp_server_skeleton.py — STUDENT SKELETON
Goal: Implement a minimal "Reliable UDP" (RUDP) server over UDP.

YOU MUST IMPLEMENT:
  1) 3-way handshake:  SYN -> (you send) SYN-ACK -> (expect) ACK
  2) DATA handling with sequence numbers + send DATA-ACK for each in-order DATA
     - maintain 'expect_seq' (next in-order sequence number you expect)
     - if out-of-order, re-ACK the last in-order seq (expect_seq - 1)
  3) Teardown: (expect) FIN -> (you send) FIN-ACK

Tips:
  - Use Wireshark with filter: udp.port == <your_assigned_port>
  - Keep the server single-client and single-threaded for simplicity.
  - Only accept packets from the first client after handshake begins.
"""
import socket, struct, random, time, argparse, sys

# ===================== CONFIG (EDIT YOUR PORT) =====================
ASSIGNED_PORT = 30147  # <-- REPLACE with your assigned UDP port
# ==================================================================

# --- Protocol type codes (1 byte) ---
SYN, SYN_ACK, ACK, DATA, DATA_ACK, FIN, FIN_ACK = 1,2,3,4,5,6,7

# Header format: type(1B) | seq(4B) | len(2B)
HDR = '!B I H'
HDR_SZ = struct.calcsize(HDR)

def pack_msg(tp: int, seq: int, payload: bytes = b'') -> bytes:
    if isinstance(payload, str):
        payload = payload.encode()
    return struct.pack(HDR, tp, seq, len(payload)) + payload

def unpack_msg(pkt: bytes):
    if len(pkt) < HDR_SZ:
        return None, None, b''
    tp, seq, ln = struct.unpack(HDR, pkt[:HDR_SZ])
    return tp, seq, pkt[HDR_SZ:HDR_SZ+ln]

def main():
    parser = argparse.ArgumentParser(description='RUDP server (with random ACK delay)')
    parser.add_argument('--port', type=int, default=ASSIGNED_PORT, help='UDP port to bind')
    parser.add_argument('--min-delay', type=int, default=100, help='Minimum ACK delay in ms')
    parser.add_argument('--max-delay', type=int, default=1000, help='Maximum ACK delay in ms')
    args = parser.parse_args()

    port = args.port
    min_delay = args.min_delay
    max_delay = args.max_delay

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    sock.bind(('0.0.0.0', port))
    print(f'[SERVER] Listening on 0.0.0.0:{port} (UDP)')
    
    client_addr = None
    established = False
    expect_seq = 0  # next in-order DATA seq we expect

    while True:
        pkt, addr = sock.recvfrom(2048)
        tp, seq, pl = unpack_msg(pkt)
        if tp is None:
            continue

        # ============ PHASE 1: HANDSHAKE (YOU IMPLEMENT) ============
        if not established:
            # If a SYN arrives, remember this client and reply with SYN-ACK
            if tp == SYN:
                client_addr = addr
                print('[SERVER] got SYN from', addr)
                synack = pack_msg(SYN_ACK, 0)
                sock.sendto(synack, client_addr)
                continue
            # If we receive final ACK from same client, finish handshake
            if tp == ACK and client_addr == addr:
                print('[SERVER] handshake complete')
                established = True
                expect_seq = 0
                continue
            # otherwise ignore other packets until handshake finishes
            continue
        # ============================================================

        # Ignore packets from other addresses once a client is set
        if client_addr is not None and addr != client_addr:
            # Optional: silently ignore or print a message
            continue

        # ============ PHASE 2: DATA (YOU IMPLEMENT) =================
        if tp == DATA:
            # In-order packet
            if seq == expect_seq:
                try:
                    txt = pl.decode()
                except Exception:
                    txt = repr(pl)
                preview = txt.replace('\n', '\\n')
                print(f'[SERVER] DATA seq={seq} delivered: {preview[:80]}')
                # Random ACK delay between configured min and max ms to induce client retransmits
                delay_ms = random.randint(min_delay, max_delay)
                time.sleep(delay_ms / 1000.0)
                ack = pack_msg(DATA_ACK, seq)
                sock.sendto(ack, client_addr)
                expect_seq += 1
            else:
                # out-of-order: re-ACK last in-order (expect_seq-1)
                last = max(0, expect_seq-1)
                ack = pack_msg(DATA_ACK, last)
                sock.sendto(ack, client_addr)
            continue
        # ============================================================

        # ============ PHASE 3: TEARDOWN (YOU IMPLEMENT) =============
        if tp == FIN:
            print('[SERVER] FIN received, closing')
            finack = pack_msg(FIN_ACK, 0)
            sock.sendto(finack, client_addr)
            # reset server state to allow another connection
            established = False
            client_addr = None
            expect_seq = 0
            continue
        # ============================================================

if __name__ == '__main__':
    main()
