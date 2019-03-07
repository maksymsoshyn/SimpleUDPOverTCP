# SimpleUDPOverTCP
Simple app that provides functionality to send data from one UDP point to another using TCP tunnel

Instruction to use main app

1.Compile repeater class with args: port for TCP
2.Compile agent class with args: repeater's IP, repeaters port, destination UDP ip, list of UDP ports on which agent will listen for messages to send them over tunnel to destination UDP point
3.Run client and server UDP app and send data to agent

Instruction for using test echo UDP app

1.Compile TestEchoClient with args: agent's ip, your port, destination port port
2.Compile TestEchoServer with args: your port


Attention: destination UDP port nad Agent port are the same
