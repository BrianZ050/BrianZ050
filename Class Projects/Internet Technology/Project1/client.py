import socket

PORT = 30147  # Assigned port number

INPUT_FILE = 'in-proj.txt'
OUTPUT_FILE = 'out-proj.txt'

def main():
    try:
        cs = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        print("[C]: Client socket created")
    except socket.error as err:
        print('socket open error: {} \n'.format(err))
        exit()

    localhost_addr = socket.gethostbyname(socket.gethostname())
    server_binding = (localhost_addr, PORT)
    cs.connect(server_binding)

    # Receive welcome message
    data_from_server = cs.recv(1024)
    print("[C]: Data received from server: {}".format(data_from_server.decode('utf-8')))

    # Open input and output files
    with open(INPUT_FILE, 'r') as infile, open(OUTPUT_FILE, 'w') as outfile:
        for line in infile:
            line = line.rstrip('\n')
            cs.send((line + '\n').encode('utf-8'))
            response = cs.recv(1024).decode('utf-8').rstrip('\n')
            print(f"[C]: Sent: {line} | Received: {response}")
            outfile.write(response + '\n')

    cs.close()
    print("[C]: Client shutdown.")

if __name__ == "__main__":
    main()
