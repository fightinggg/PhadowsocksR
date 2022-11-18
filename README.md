# PhadowsocksR
Polin'S ShadowsocksR, Support For Http Proxy and Socket5 Proxy

# Witch Protocol Support Now?
- [x] socks5 protocol with no authentication
- [x] http protocol with no authentication
- [x] https protocol with no authentication
- [ ] socks5 protocol username password authentication
- [ ] http protocol username password authentication
- [ ] https protocol username password authentication


[comment]: <> (# How To Use)

[comment]: <> (```shell)

[comment]: <> (docker run -d -p 1080:1080 fightinggg/psr:master )

[comment]: <> (```)

[comment]: <> (# How To Debug)

[comment]: <> (```shell)

[comment]: <> (docker run -it -rm -p 1080:1080 fightinggg/psr:master \)

[comment]: <> (java -Dlevel=debug \)

[comment]: <> (-jar /app/psr.jar)

[comment]: <> (```)


[comment]: <> (```shell)

[comment]: <> (java -jar psr.jar --port 1080 --protocol http)

[comment]: <> (java -jar psr.jar --port 1080 --protocol socks5)

[comment]: <> (java -jar psr.jar)

[comment]: <> (```)
