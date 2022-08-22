# Technical notes

## Add new game

### JVM

Using `fp` as an example
```shell
cd src/main/java/com/faendir/zachtronics/bot/
cp -r sz fp
find fp -name 'Sz*' | xargs file-rename 's/Sz/Fp/' {}
find fp -type f | xargs rpl 'Sz' 'Fp' {}
find fp -type f | xargs rpl '\.sz' '.fp' {}
```

Edit:
* `discord/FpCommand`
* `model/*`
* `repository/*`
* `rest/dto/*`

Add `lombok.copyableannotations += com.faendir.zachtronics.bot.fp.FpQualifier` to `/lombok.config`

### Git

Create git repo in game org.

Add the push webhook:
* Payload URL: https://zlbb.faendir.com/push
* Content type: `application/json`
* Secret: GPG-encoded file

Give `F43nd1r` Write permissions if he's not an admin of the particular org.

Add to the `GitConfiguration` class.

Add skeleton to `test/resources`, and add to the `TestConfiguration` class for tests

### FE

### Reddit

Take hold of the subreddit wiki
