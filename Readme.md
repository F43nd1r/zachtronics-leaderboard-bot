# Zachtronics Bot

A discord & reddit bot for automating solution submission/display for [Zachtronics](http://www.zachtronics.com/) games.

Currently included:
 - Opus Magnum
 - SpaceChem (read-only)
 
# Build
0. Set up docker-compose, create a discord bot, create a reddit app, get a github personal access token. Follow respective guides.

1. Run gradle:
```
./gradlew build
``` 
2. Run via docker-compose:
```yaml
version: "3.7"

services:
  om-discord-bot:
    build: .
    container_name: om-discord-bot
    environment:
      JDA_TOKEN: abcdef
      GIT_USERNAME: xyz
      GIT_ACCESS_TOKEN: abcdef
      REDDIT_USERNAME: xyz
      REDDIT_ACCESS_TOKEN: abcdef
      REDDIT_CLIENT_ID: qwert
      REDDIT_PASSWORD: yxcvb
```
Alternatively to `build .` you can use prebuilt `image: f43nd1r/om-leaderboard-discord-bot:latest`. This makes the gradle build obsolete.

---

The used github repositories are hardcoded into each leaderboard. You'll need to change those if the supplied github account does not have access to the original ones.
