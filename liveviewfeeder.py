__author__ = 'Hibiki'

import socket

# define source image path
img_loc = "/dev/shm/mjpeg/cam.jpg";

# define host and port
HOST = "0.0.0.0"
PORT = 5000

def main():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    s.bind((HOST, PORT))
    s.listen(1)

    alive = True

    while alive:
        try:
            (c, a) = s.accept()
            handle_image_req(c, a)
        except KeyboardInterrupt:
            print("Shutting down LiveView Feeder ...")
            alive = False
            s.close()


def handle_image_req(c, a):
    # DEBUG
    print(a)
    # open file in `binary` read mode
    f = open(img_loc, 'rb')
    # read file contents
    bin_data = f.read()
    # send to client
    c.sendall(bytes(bin_data))
    # terminate connection and clean up
    c.shutdown(socket.SHUT_WR)
    c.close()
    f.close()


if __name__ == "__main__":
    main()
