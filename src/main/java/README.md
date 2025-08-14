# Technical notes

## Add new game

### JVM

Using `sz` -> `cw` as an example
```shell
cd src/main/java/com/faendir/zachtronics/bot/
cp -r sz cw
find cw -name 'Sz*' | xargs file-rename 's/Sz/Cw/' {}
find cw -type f | xargs rpl 'Sz' 'Cw' {}
find cw -type f | xargs rpl '\.sz' '.cw' {}
find cw -type f | xargs rpl '/sz' '/cw' {}
find cw -type f | xargs rpl '"sz"' '"cw"' {}
cd -
cd src/test/java/com/faendir/zachtronics/bot/
cp -r sz cw
find cw -name 'Sz*' | xargs file-rename 's/Sz/Cw/' {}
find cw -type f | xargs rpl 'Sz' 'Cw' {}
find cw -type f | xargs rpl '\.sz' '.cw' {}
```

Edit:
* `discord/CwOptionBuilders`
* `model/*`
* `repository/*`
* `rest/dto/*`

Add `lombok.copyableannotations += com.faendir.zachtronics.bot.cw.CwQualifier` to `/lombok.config`

### Git

#### GitHub

Create git repo in game org.

Add the push webhook:
* Payload URL: https://zlbb.faendir.com/push
* Content type: `application/json`
* Secret: GPG-encoded file at https://discord.com/channels/747474678498721994/747474678498721997/885673075255943198

Give `F43nd1r` Write permissions if he's not an admin of the particular org.

#### Project

* Add to the `GitConfiguration` class.
* Add skeleton to `test/resources`, and add to the `TestConfiguration` class for tests.
* Create a submit test that exercises the sim
* Update `/Readme.md` with the new game

#### Repo

Add README and LICENSE
Fill levels with the `initLeaderboard()` test.

### Docker

Add simulator (if any) to `/Dockerfile`

### FE

* Add route to `web/src/index.tsx`
* Add score class to `web/src/model/<game>`
* Add view to `web/src/views/<game>`

### Reddit

* Take hold of the subreddit wiki, map it in `Subreddit.kt`
* Create `src/test/resources/reddit/<subreddit>/wiki/<page>.md`
* Copy the prefix from an existing lb, tweak as desired
* Generate an empty table via `createWiki` test method
* Create the wiki page on reddit
* Test, then paste the content on the reddit page