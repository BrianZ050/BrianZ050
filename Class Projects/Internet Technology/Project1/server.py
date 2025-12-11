import socket

PORT = 30147  # Assigned port number

def process_message(msg):
    # Reverse the string and swap case
    return msg[::-1].swapcase()

def main():
    try:
        ss = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print("[S]: Server socket created")
    except socket.error as err:
        print('socket open error: {}\n'.format(err))
        exit()

    server_binding = ('', PORT)
    ss.bind(server_binding)
    ss.listen(1)
    host = socket.gethostname()
    print("[S]: Server host name is {}".format(host))
    localhost_ip = (socket.gethostbyname(host))
    print("[S]: Server IP address is {}".format(localhost_ip))
    print(f"[S]: Listening on port {PORT}")

    csockid, addr = ss.accept()
    print ("[S]: Got a connection request from a client at {}".format(addr))

    # Send welcome message
    msg = "Welcome to CS 352!"
    csockid.send(msg.encode('utf-8'))

    # Now receive lines, process, and send back
    while True:
        data = csockid.recv(1024)
        if not data:
            break
        line = data.decode('utf-8').rstrip('\n')
        print(f"[S]: Received: {line}")
        response = process_message(line)
        csockid.send((response + '\n').encode('utf-8'))
        print(f"[S]: Sent: {response}")

    csockid.close()
    ss.close()
    print("[S]: Server shutdown.")

if __name__ == "__main__":
    main()
