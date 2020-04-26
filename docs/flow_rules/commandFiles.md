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

* ens4: 6
* ens5: 3
* ens6: 4

```bash
#New Version
table=0,in_port=3,actions=set_field:100->tun_id,resubmit(,1) #281478658103404
table=0,in_port=4,actions=set_field:200->tun_id,resubmit(,1) #281478918241054
table=0, actions=resubmit(,1)
#281478693229640
table=1,tun_id=100,eth_dst=00:50:79:66:68:05,actions=output:3 #281476851398593
table=1,tun_id=200,eth_dst=00:50:79:66:68:06,actions=output:4 #281477833129373
table=1,tun_id=100,eth_dst=00:50:79:66:68:03,actions=output:6 #281478753381088
table=1,tun_id=200,eth_dst=00:50:79:66:68:04,actions=output:6 #281476547375381
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.1,actions=output:3
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.3,actions=output:4
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.2,actions=output:6
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.4,actions=output:6
table=1,priority=100,actions=drop
#281476103729636

#Old Version
table=0,in_port=3,actions=set_field:100->tun_id,resubmit(,1)
table=0,in_port=4,actions=set_field:200->tun_id,resubmit(,1)
table=0, actions=resubmit(,1)
table=1,tun_id=100,dl_dst=00:50:79:66:68:05,actions=output:3
table=1,tun_id=200,dl_dst=00:50:79:66:68:06,actions=output:4
table=1,tun_id=100,dl_dst=00:50:79:66:68:03,actions=output:6
table=1,tun_id=200,dl_dst=00:50:79:66:68:04,actions=output:6
table=1,tun_id=100,arp,nw_dst=172.64.0.1,actions=output:3
table=1,tun_id=200,arp,nw_dst=172.64.0.3,actions=output:4
table=1,tun_id=100,arp,nw_dst=172.64.0.2,actions=output:6
table=1,tun_id=200,arp,nw_dst=172.64.0.4,actions=output:6
table=1,priority=100,actions=drop
```

### OVS2

Information using the `sudo ovs-ofctl show br-1`

* ens4: 5
* ens5: 3
* ens6: 2

```bash
#New Version
table=0,in_port=3,actions=set_field:100->tun_id,resubmit(,1)#281478593351756
table=0,in_port=2,actions=set_field:200->tun_id,resubmit(,1) #281476953122511
table=0, actions=resubmit(,1)
#281477189538230
table=1,tun_id=100,eth_dst=00:50:79:66:68:03,actions=output:3 #281477976136367
table=1,tun_id=200,eth_dst=00:50:79:66:68:04,actions=output:2 #281478476854409
table=1,tun_id=100,eth_dst=00:50:79:66:68:05,actions=output:5 #281477062007166
table=1,tun_id=200,eth_dst=00:50:79:66:68:06,actions=output:5 #281478580891175
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.2,actions=output:3
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.4,actions=output:2
table=1,tun_id=100,eth_type=0x0806,arp_tpa=172.64.0.1,actions=output:5
table=1,tun_id=200,eth_type=0x0806,arp_tpa=172.64.0.3,actions=output:5
table=1,priority=100,actions=drop
#281477753240997

#Old Version
table=0,in_port=3,actions=set_field:100->tun_id,resubmit(,1)
table=0,in_port=2,actions=set_field:200->tun_id,resubmit(,1)
table=0, actions=resubmit(,1)
table=1,tun_id=100,dl_dst=00:50:79:66:68:03,actions=output:3
table=1,tun_id=200,dl_dst=00:50:79:66:68:04,actions=output:2
table=1,tun_id=100,dl_dst=00:50:79:66:68:05,actions=output:5
table=1,tun_id=200,dl_dst=00:50:79:66:68:06,actions=output:5
table=1,tun_id=100,arp,nw_dst=172.64.0.2,actions=output:3
table=1,tun_id=200,arp,nw_dst=172.64.0.4,actions=output:2
table=1,tun_id=100,arp,nw_dst=172.64.0.1,actions=output:5
table=1,tun_id=200,arp,nw_dst=172.64.0.3,actions=output:5
table=1,priority=100,actions=drop
```
