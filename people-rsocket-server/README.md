##people-rsocket-server

rsc tcp://localhost:8888 -sm simple:user:{noop}pwd --smmt message/x.rsocket.authentication.v0 -r people-read-32
rsc tcp://localhost:8888 -sm simple:user:{noop}pwd --smmt message/x.rsocket.authentication.v0 --stream -r people-read

