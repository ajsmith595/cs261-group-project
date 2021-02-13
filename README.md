# CS261 Group Project

Group project for CS261

## Links

Google Doc: https://docs.google.com/document/d/1i3FmGXvaAwfaQuTXZKVt_jIk44qH4fHwjytNrjMVeFY/edit?usp=sharing

Overleaf Doc: https://www.overleaf.com/2214364475qmkzvmxxmnbk

MongoDB: https://www.mongodb.com/try/download/community

## Database

For the db to work, install MongoDB (link above) and have it running before you start the server.

## Examples

API responses always comes back as either
```
{"status":"success","data":{DATA}}
```
or
```
{"status":"error","message":"ERROR MESSAGE"}
```

Event POST request (using curl)
```
curl -X POST "http://localhost:4567/api/events" -H "Content-Type: application/json" -d '{"hostID":"0","templateID":"0","title":"Example Event","startTime":1613148624671,"duration":30}'
```

Event GET request (using curl)
```
curl -X GET "http://localhost:4567/api/event/1LK15" -H "Content-Type: application/json" -H "Accept-Type: application/json"
```
(note: you will need to change `1Lk15` to whatever event code was given from the POST request).