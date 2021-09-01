# PhadowsocksR
Polin'S ShadowsocksR, Support For Http Proxy and Socket5 Proxy

# Witch Protocol Support Now?
- [x] socks5 protocol with no authentication
- [ ] socks5 protocol username password authentication
- [ ] http protocol
- [ ] https protocol

# How To Use
```shell
docker run -d -p 1080:1080 fightinggg/psr:master
```

# How To Debug
```shell
docker run -it -rm -p 1080:1080 fightinggg/psr:master \
java -Dlevel=debug \
-jar /app/psr.jar
```

