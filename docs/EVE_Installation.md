# Pasos hechos para poder hacer una comunicación

- Activar la opciónde IP FORWARDING para la red principal de GCP
- Añadir la red interna 10.230.0.0 para tener otra interfaz con la cual se pueda trabajar
- Instalación normal de la máquina según las instrucciones de [Google Install EVE-NG](https://www.eve-ng.net/index.php/documentation/installation/google-cloud-install/)
- Conectar el puente pnet1 a la red porque no tiene el mismo nombre que espera EVE-NG, esto se hace entrando al archivo con sudo y cambiando eth1 por ens5

```bash
sudo nano /etc/network/interfaces
# Dentro del archivo cambiar eth1 por ens5
```

- Habilitar comunicación IP Forwarding

```bash
sudo nano /etc/sysctl.conf
# Dentro del arhivo descomentar la línea
net.ipv4.ip_forward=1
```

- Reiniciar el sistema para que se apliquen los cambios

- Conectar en el ambiente EVE-NG el cloud1 para poder hacer la comunicación con el OpenvSwitch. Esto va a permitir que la red vunl0_1_0 aparezca conectada al puente **pnet1**. 
- Usar el modo promiscuo en la interfaz **pnet1**

```bash
sudo ifconfig pnet1 promisc up
```

- Darle la IP de ens5 al bridge usando el comando `ifconfig` y eliminar la IP del `ens5`.

```bash
sudo ip addr flush dev ens5
ifconfig pnet1 10.235.0.2 netmask 255.255.240.0
```

- Se habilita el HOST como un NAT que va a emascarar todo lo que entre con la IP que tiene la interfaz `eth0`. 

```bash
iptables -t nat -A POSTROUTING -o pnet1 -j MASQUERADE
```

Dentro de la máquina en EVE-NG OpenvSwitch_1 se hacen los siguientes cambios

- Ponerle una IP dentro de la subnet que tiene `pnet1`.

```bash
sudo ifconfig ens3 10.235.0.10/20
```

- Dentro de Google Shell se tiene que poner la ruta de esta forma para que todo se redirija a ONOS `(IP=10.128.0.4)`

```bash
gcloud compute routes create eve1 \
    --destination-range=10.235.0.0/20 \
    --network=default \
    --next-hop-address=10.128.0.4
```
