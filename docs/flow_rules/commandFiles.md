# Command to work with the EVE LAB

## Comando para reiniciar el servicio de ONOS

```bash
sudo ./onos-service clean
```

## FRRouting Commands

```bash
sudo su -
ip addr flush dev ens3
ip addr flush dev ens4
## Primera Interfaz OVS 1
ip addr add 192.168.10.1/24 dev ens4
ip link set ens4 up
ip route add 192.168.10.0/24 via 192.168.10.5
## Primera Interfaz OVS 2
ip addr add 192.168.20.1/24 dev ens3
ip link set ens3 up
ip route add 192.168.20.0/24 via 192.168.20.5
```

## OVS 1 Commands to make the connection

```bash
sudo su -
# Connect to the manager ONOS (IP=10.128.15.212)
ovs-vsctl set-manager tcp:10.128.15.212:6640
# Crear la conexion con el router
ip addr flush dev ens4
ip addr add 192.168.10.5/24 dev ens4
ip route add 192.168.20.0/24 via 192.168.10.1
```

## OVS 2 Commands to make the connection

```bash
sudo su -
## Conectar el manager a ONOS (IP=10.128.15.212)
ovs-vsctl set-manager tcp:10.128.15.212:6640
## Crear la conexion con el router
ip addr flush dev ens4
ip addr add 192.168.20.5/24 dev ens4
ip route add 192.168.10.0/24 via 192.168.20.1
```

## Changes in Google Cloud Shell to make the ip routes

```bash
gcloud compute routes create eve instances \
    --destination-range=10.235.0.0/20 \
    --network=default \
    --next-hop-address=10.128.15.213
```

## Flow Rules

### Basic Openflow OVS Commands

```bash
sudo ovs-ofctl show br-1
sudo ovs-appctl bridge/dump-flows br-1
sudo ovs-ofctl add-flows br-2 flows2.txt
sudo ovs-ofctl dump-flows br-2
sudo ovs-vsctl set bridge br-1 protocols=OpenFlow10,OpenFlow11,OpenFlow12,OpenFlow13,OpenFlow14,OpenFlow15
sudo ovs-ofctl show br-2
sudo ovs-vsctl add-port br0 vxlan1 -- set interface vxlan1 type=vxlan options:remote_ip=192.168.1.2 options:key=flow options:dst_port=8472
```

### OVS1

Information using the `sudo ovs-ofctl show br-1`

* ens5:     1
* ens6:     2
* vxlan1:   3 - MPLS
* vxlan2:   4 - Internet/LTE

Host1: 00:50:79:66:68:04 id 100 172.64.0.1
Host2: 00:50:79:66:68:0a id 200 172.64.0.3
Host3: 00:50:79:66:68:06 id 100 172.64.0.2
Host4: 00:50:79:66:68:07 id 200 172.64.0.4

```bash
#New Version
table=0,in_port=1,actions=set_field:100->tun_id,resubmit(,1)
table=0,in_port=2,actions=set_field:200->tun_id,resubmit(,1) 
table=0, actions=resubmit(,1)
table=1,tun_id=100,eth_dst=00:50:79:66:68:04,actions=output:1
table=1,tun_id=200,eth_dst=00:50:79:66:68:0a,actions=output:2
table=1,tun_id=100,eth_dst=00:50:79:66:68:06,actions=output:3
table=1,tun_id=200,eth_dst=00:50:79:66:68:07,actions=output:3
table=1,tun_id=100,eth_dst=00:50:79:66:68:06,actions=output:4 #LowPriority
table=1,tun_id=200,eth_dst=00:50:79:66:68:07,actions=output:4 #LowPriority
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.1,actions=output:1
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.3,actions=output:2
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.2,actions=output:3
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.4,actions=output:3
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.2,actions=output:4 #LowPriority
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.4,actions=output:4 #LowPriority
#LowPriority
table=1,priority=100,actions=drop
```

### OVS2

Information using the `sudo ovs-ofctl show br-1`

* ens5:     1
* ens6:     2
* vxlan1:   3
* vxlan2:   4

Host1: 00:50:79:66:68:04 id 100 172.64.0.1
Host2: 00:50:79:66:68:0a id 200 172.64.0.3
Host3: 00:50:79:66:68:06 id 100 172.64.0.2
Host4: 00:50:79:66:68:07 id 200 172.64.0.4

```bash
#New Version
table=0,in_port=1,actions=set_field:100->tun_id,resubmit(,1)
table=0,in_port=2,actions=set_field:200->tun_id,resubmit(,1)
table=0, actions=resubmit(,1)
table=1,tun_id=100,eth_dst=00:50:79:66:68:06,actions=output:1
table=1,tun_id=200,eth_dst=00:50:79:66:68:07,actions=output:2
table=1,tun_id=100,eth_dst=00:50:79:66:68:04,actions=output:3
table=1,tun_id=200,eth_dst=00:50:79:66:68:0a,actions=output:3
table=1,tun_id=100,eth_dst=00:50:79:66:68:04,actions=output:4 #LowPriority
table=1,tun_id=200,eth_dst=00:50:79:66:68:0a,actions=output:4 #LowPriority
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.2,actions=output:1
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.4,actions=output:2
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.1,actions=output:3
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.3,actions=output:3
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.1,actions=output:4 #LowPriority
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.3,actions=output:4 #LowPriority
table=1,priority=100,actions=drop
```

