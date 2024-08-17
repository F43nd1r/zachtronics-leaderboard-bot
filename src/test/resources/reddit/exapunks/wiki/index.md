# Exapunks leaderboard
This page displays the best known solutions to all official levels. It is maintained by a [bot](https://github.com/F43nd1r/zachtronics-leaderboard-bot), which collects submitted solutions into a [repository](https://github.com/12345ieee/exapunks-leaderboard) and generates the page seen here.

### Submitting and accessing solutions
The 📄 icons are links to the solution files, which can be opened in-game.

To add your solution to the bot, join the discord server at https://discord.gg/98QNzdJ and invoke the Leaderboard bot via `/exa submit solution:<export link> author:<your name> cheesy:<True/False>`.  
Use `m1` for the solution link to refer to the preceding message if that's how you uploaded the export.  
You can use the optional `image` parameter to give a gif of your solution.

If you want to import a previously stored solution into your own game, you can find it by following the relevant link in this table (if it is a record solution); or by going to the Discord Server and invoking the Leaderboard Bot via `/exa frontier puzzle:<puzzle name>`; or by going to the leaderboard git repository and navigating to the campaign, level and solution you want.  
The files can be copied directly into the game's save folder and opened in-game.

### Regarding cheesy solutions

A solution is considered *cheesy* if it skips doing an intended part of the puzzle by exploiting information that is supposed to be irrelevant ("junk data"). Cheesy solutions are disqualified from the main leaderboards, and instead have their own leaderboards at the bottom.

Note that taking advantage of features common to all 100 test runs is not considered cheesy. For example, in the first Snaxnet puzzle, the word `PEANUTS` is always in the first 11 entries, so it’s okay to only search the first 11 entries.

There are several possible sources of junk data:
1. Files or registers that are only there for decorative or world-building purposes (present in most levels)
2. Randomly-generated hostnames (present in APL, Mitsuzen levels, and Modem levels)
3. Input files that contain junk data along with the useful data (for example Holman)

1 is the most common case, and is pretty clear-cut. A legit solution has no reason to interact with those files or registers at all, so a solution that requires those to work is always cheesy. For example, in Pizza, file `280` (an ad) contains the word `CHEESE`. Pulling the word `CHEESE` from that ad can be easier than pulling it from the order as intended, but it makes the solution cheesy.

2 and 3 are harder to judge. Even non-cheesy solutions often interact with those kinds of junk data. A solution is only cheesy if it uses that junk data as a source of information that lets it skip doing the intended work.

### Main Campaign

| Level | Cycles | Size | Activity
| ---  | ---  | --- | ---
| [TW1](https://zlbb.faendir.com/exa/PB000) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB000/trash-world-news-4-3-2.solution) [**4**/3/2 Community](https://i.imgur.com/lS86STl.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB000/trash-world-news-4-3-2.solution) [4/**3**/2 Community](https://i.imgur.com/lS86STl.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB000/trash-world-news-4-3-2.solution) [4/3/**2** Community](https://i.imgur.com/lS86STl.gif)
|
| [TW2](https://zlbb.faendir.com/exa/PB001) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB001/trash-world-news-7-6-2.solution) [**7**/6/2 Community](https://i.imgur.com/DyuqycY.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB001/trash-world-news-7-6-2.solution) [7/**6**/2 Community](https://i.imgur.com/DyuqycY.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB001/trash-world-news-7-6-2.solution) [7/6/**2** Community](https://i.imgur.com/DyuqycY.gif)
|
| [TW3](https://zlbb.faendir.com/exa/PB037) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB037/trash-world-news-9-12-4.solution) [**9**/12/4 Community](https://i.imgur.com/mlLvswf.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB037/trash-world-news-9-12-4.solution) [9/**12**/4 Community](https://i.imgur.com/mlLvswf.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB037/trash-world-news-10-14-3.solution) [10/14/**3** Community](https://i.imgur.com/9fstmFB.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB037/trash-world-news-11-13-3.solution) [11/13/**3** Grimmy](https://i.imgur.com/ZMbaIuu.gif)
|
| [TW4](https://zlbb.faendir.com/exa/PB002) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB002/trash-world-news-111-50-6.solution) [**111**/50/6 Community](https://i.imgur.com/bTdIR8c.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB002/trash-world-news-306-10-2.solution) [306/**10**/2 Grimmy](https://i.imgur.com/FYsQcFN.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB002/trash-world-news-113-48-2.solution) [113/48/**2** Grimmy](https://i.imgur.com/vRVi8XZ.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB002/trash-world-news-306-10-2.solution) [306/10/**2** Grimmy](https://i.imgur.com/FYsQcFN.gif)
|
| [Pizza](https://zlbb.faendir.com/exa/PB003B) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-12-21-1.solution) [**12**/21/1 Community](https://i.imgur.com/mctJbNU.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-24-11-5.solution) [24/**11**/5 Community](https://i.imgur.com/aOyZ35G.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-12-21-1.solution) [12/21/**1** Community](https://i.imgur.com/mctJbNU.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-19-12-1.solution) [19/12/**1** Community](https://i.imgur.com/0fm4un6.gif)
|
| [Left Arm](https://zlbb.faendir.com/exa/PB004) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB004/mitsuzen-hdi10-36-15-72.solution) [**36**/15/72 Community](https://i.imgur.com/aVQJcwM.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB004/mitsuzen-hdi10-74-9-133.solution) [74/**9**/133 Grimmy](https://i.imgur.com/14XBGdN.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB004/mitsuzen-hdi10-38-39-5.solution) [38/39/**5** Grimmy](https://i.imgur.com/FJuDmAm.gif)
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB004/mitsuzen-hdi10-36-47-12.solution) [**36**/47/12 Grimmy](https://i.imgur.com/yrkusLT.gif) |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB004/mitsuzen-hdi10-124-14-5.solution) [124/14/**5** Grimmy](https://i.imgur.com/TI7oQoO.gif)
|
| [Snaxnet 1](https://zlbb.faendir.com/exa/PB005) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB005/last-stop-snaxnet-25-48-5.solution) [**25**/48/5 Community](https://i.imgur.com/1ZZbTwk.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB005/last-stop-snaxnet-29-11-2.solution) [29/**11**/2 Community](https://i.imgur.com/oY9joBG.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB005/last-stop-snaxnet-27-29-2.solution) [27/29/**2** Grimmy](https://i.imgur.com/nNSRBq1.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB005/last-stop-snaxnet-29-11-2.solution) [29/11/**2** Community](https://i.imgur.com/oY9joBG.gif)
|
| [Zebros](https://zlbb.faendir.com/exa/PB006B) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB006B/zebros-copies-52-50-9.solution) [**52**/50/9 Bacardi](https://i.imgur.com/z1yCXLI.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB006B/zebros-copies-108-22-3.solution) [108/**22**/3 Grimmy](https://i.imgur.com/jgWDDkf.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB006B/zebros-copies-55-32-2.solution) [55/32/**2** Grimmy](https://i.imgur.com/Zn4BkaX.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB006B/zebros-copies-99-25-2.solution) [99/25/**2** Grimmy](https://i.imgur.com/yoSZrB2.gif)
|
| [Highway](https://zlbb.faendir.com/exa/PB007) | [**71**/50/24 realMrLucky/Wizou](https://i.imgur.com/eiJq8XE.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB007/sfcta-highway-sign-#4902-140-10-1.solution) [140/**10**/1 Grimmy](https://i.imgur.com/ANAuspD.gif) | [74/44/**1** repoBoy](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/HIGHWAY/74%7C44%7C1)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB007/sfcta-highway-sign-#4902-140-10-1.solution) [140/10/**1** Grimmy](https://i.imgur.com/ANAuspD.gif)
|
| [UN1](https://zlbb.faendir.com/exa/PB008) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB008/unknown-network-15-26-27.solution) [**15**/26/27 Community](https://i.imgur.com/IyZa6ZN.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB008/unknown-network-144-13-153.solution) [144/**13**/153 Grimmy](https://i.imgur.com/JFp8uLY.mp4?loop=1) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB008/unknown-network-15-26-27.solution) [15/26/**27** Community](https://i.imgur.com/IyZa6ZN.gif)
|  |  | 348/**13**/104 Brian142857 | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB008/unknown-network-34-14-27.solution) [34/14/**27** Grimmy](https://i.imgur.com/i1QLvTP.gif)
|
| [Berkeley](https://zlbb.faendir.com/exa/PB009) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB009/uc-berkeley-69-75-11.solution) [**69**/75/11 Community](https://i.imgur.com/LTPIjgz.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB009/uc-berkeley-385-26-9.solution) [385/**26**/9 Grimmy](https://i.imgur.com/NtuMvAD.gif) | [75/75/**7** shuffleskye](https://i.imgur.com/gqNLvqh.gif)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB009/uc-berkeley-402-26-7.solution) [402/**26**/7 Grimmy](https://i.imgur.com/rcZ0jiT.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB009/uc-berkeley-402-26-7.solution) [402/26/**7** Grimmy](https://i.imgur.com/rcZ0jiT.gif)
|
| [Workhouse](https://zlbb.faendir.com/exa/PB010B) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB010B/workhouse-169-75-4.solution) [**169**/75/4 Bacardi](https://i.imgur.com/pQgMcmP.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB010B/workhouse-976-19-2.solution) [976/**19**/2 Grimmy](https://i.imgur.com/1tPlVLp.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB010B/workhouse-178-75-2.solution) [178/75/**2** Grimmy](https://i.imgur.com/LsX5QLq.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB010B/workhouse-976-19-2.solution) [976/19/**2** Grimmy](https://i.imgur.com/1tPlVLp.mp4)
|
| [Bank 1](https://zlbb.faendir.com/exa/PB012) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB012/equity-first-bank-1056-50-10.solution) [**1056**/50/10 Community](https://i.imgur.com/IqcTdUj.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB012/equity-first-bank-1620-12-10.solution) [1620/**12**/10 Community](https://i.imgur.com/Fu9dItG.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB012/equity-first-bank-1056-50-10.solution) [1056/50/**10** Community](https://i.imgur.com/IqcTdUj.mp4)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB012/equity-first-bank-1620-12-10.solution) [1620/12/**10** Community](https://i.imgur.com/Fu9dItG.mp4)
|
| [Heart](https://zlbb.faendir.com/exa/PB011B) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB011B/mitsuzen-hdi10-36-50-67.solution) [**36**/50/67 Bacardi](https://i.imgur.com/5qBuPf8.gif) | [912/**14**/121 PentaPig](https://i.imgur.com/eZJFpLE.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB011B/mitsuzen-hdi10-49-43-5.solution) [49/43/**5** Grimmy](https://i.imgur.com/lbeXEgM.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB011B/mitsuzen-hdi10-98-21-5.solution) [98/21/**5** Grimmy](https://i.imgur.com/n17s9vP.gif)
|
| [TW5](https://zlbb.faendir.com/exa/PB013C) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB013C/trash-world-news-494-50-152.solution) [**494**/50/152 Bacardi](https://i.imgur.com/8RVjX2E.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB013C/trash-world-news-555-18-12.solution) [555/**18**/12 Community](https://i.imgur.com/8xzRV5t.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB013C/trash-world-news-504-50-3.solution) [504/50/**3** Bacardi](https://i.imgur.com/6Nd8K4e.gif)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB013C/trash-world-news-560-18-11.solution) [560/**18**/11 Grimmy](https://i.imgur.com/0LRl76d.gif) | [547/21/**3** DarkMatter\_Zombie](https://i.imgur.com/jEcnAdB.gifv)
|
| [Redshift](https://zlbb.faendir.com/exa/PB015) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB015/tec-redshift-366-50-140.solution) **366**/50/140 Bacardi | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB015/tec-redshift-12913-18-3229.solution) [12913/**18**/3229 Grimmy](https://i.imgur.com/76aUJvm.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB015/tec-redshift-1943-50-2.solution) [1943/50/**2** Grimmy](https://i.imgur.com/ihTKVOk.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB015/tec-redshift-5788-20-2.solution) [5788/20/**2** Community](https://i.imgur.com/6uOHeEd.gif)
|
| [Library](https://zlbb.faendir.com/exa/PB016) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB016/digital-library-project-151-75-137.solution) **151**/75/137 Bacardi | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB016/digital-library-project-1172-22-50.solution) [1172/**22**/50 Grimmy](https://youtu.be/TwFHdvBtu2Y) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB016/digital-library-project-343-67-10.solution) [343/67/**10** Grimmy](https://i.imgur.com/6Rer3IF.mp4)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB016/digital-library-project-1297-33-10.solution) [1297/33/**10** Grimmy](https://i.imgur.com/xrqHVpO.mp4)
|
| [Modem 1](https://zlbb.faendir.com/exa/PB040) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB040/tec-exablaster-modem-119-54-27.solution) [**119**/54/27 Community](https://i.imgur.com/72MvcQR.gif) | [575/**23**/9 DarkMatter\_Zombie](https://i.redd.it/t2qx9xx01x181.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB040/tec-exablaster-modem-121-59-9.solution) [121/59/**9** Grimmy](https://i.imgur.com/eUW340p.gif)
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB040/tec-exablaster-modem-119-58-10.solution) [**119**/58/10 Grimmy](https://i.imgur.com/NRBoE3y.gif) |  | [575/23/**9** DarkMatter\_Zombie](https://i.redd.it/t2qx9xx01x181.gif)
|
| [Emersons](https://zlbb.faendir.com/exa/PB018) | [**20**/70/8 aischarm](https://i.imgur.com/9Q9L95L) | [53/**18**/30 ErmaTheElf](https://i.imgur.com/pIZBfCE.gif) | 21/52/**6** aischarm
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB018/emersons-guide-48-27-6.solution) [48/27/**6** Grimmy](https://i.imgur.com/X0Qe4JV.gif)
|
| [Left Hand](https://zlbb.faendir.com/exa/PB038) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB038/mitsuzen-hdi10-24-40-80.solution) [**24**/40/80 Grimmy](https://i.imgur.com/WJd3kYj.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB038/mitsuzen-hdi10-210010-14-70128.solution) 210010/**14**/70128 Grimmy | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB038/mitsuzen-hdi10-37-35-9.solution) [37/35/**9** Grimmy](https://i.imgur.com/rwmV7cN.gif)
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB038/mitsuzen-hdi10-24-56-46.solution) [**24**/56/46 Grimmy](https://i.imgur.com/SxzoPti.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB038/mitsuzen-hdi10-324625-14-127.solution) 324625/**14**/127 Grimmy | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB038/mitsuzen-hdi10-62-17-9.solution) [62/17/**9** Grimmy](https://i.imgur.com/ACuJ5JL.gif)
|
| [Sawayama](https://zlbb.faendir.com/exa/PB020) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB020/sawayama-wonderdisc-1344-100-1207.solution) [**1344**/100/1207 Bacardi](https://cdn.discordapp.com/attachments/469290305292730368/851659880503050260/SPOILER_EXAPUNKS_-_Sawayama_WonderDisc_1344_100_1207_2021-06-08-05-10-59.gif) | [38174/**31**/94 Grimmy](https://media.discordapp.net/attachments/469290305292730368/1037234578131255336/EXAPUNKS_-_Sawayama_WonderDisc_38174_31_94_2022-11-02-06-17-21.gif) | [2442/100/**3** Theophiphile](https://i.imgur.com/Ot795jl.gifv)
|  |  |  | [6659/44/**3** shuffleskye](https://i.imgur.com/uo16zys.gif)
|
| [APL](https://zlbb.faendir.com/exa/PB021) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB021/alliance-power-and-light-17-39-27.solution) [**17**/39/27 Grimmy](https://i.imgur.com/yPQeW9X.gif) | [84/**15**/96 repoBoy](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/APL/84%7C15%7C96) | [20/31/**20** Snowball](https://i.imgur.com/SthDqFW.gifv)
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB021/alliance-power-and-light-17-42-26.solution) [**17**/42/26 Grimmy](https://i.imgur.com/drPEJ2w.gif) |  | [25/22/**20** Snowball](https://i.imgur.com/ZJ0Ypcz.gifv)
|
| [XLB](https://zlbb.faendir.com/exa/PB023) | [**46**/100/7 Bacardi](https://i.imgur.com/NBsbPjX.gif) | [1782/**28**/11 shuffleskye](https://i.imgur.com/Xxy3UZQ.gif) | [54/93/**1** aischarm](https://i.imgur.com/D7wbfaX)
|  |  |  | [2069/31/**1** shuffleskye](https://i.imgur.com/8cGDpUG.gif)
|
| [KRO](https://zlbb.faendir.com/exa/PB024) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB024/kings-ransom-online-26-77-34.solution) [**26**/77/34 Grimmy](https://cdn.discordapp.com/attachments/469290305292730368/752657689578045501/EXAPUNKS_-_Kings_Ransom_Online_26_77_34_2020-09-08-02-31-53.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB024/kings-ransom-online-9614-17-5622.solution) [9614/**17**/5622 Grimmy](https://www.youtube.com/watch?v=YpENBEcEkHM) | [191/98/**24** TheImmortalSmoke](https://imgur.com/a/PooOf3w)
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB024/kings-ransom-online-26-84-33.solution) [**26**/84/33 Grimmy](https://cdn.discordapp.com/attachments/469290305292730368/752657652454260786/EXAPUNKS_-_Kings_Ransom_Online_26_84_33_2020-09-08-02-32-25.gif) |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB024/kings-ransom-online-4863-46-24.solution) [4863/46/**24** Grimmy](https://www.youtube.com/watch?v=fxmMF-5W128)
|
| [KGOG](https://zlbb.faendir.com/exa/PB028) | [**76**/99/48 Bacardi](https://i.imgur.com/FScp9Om.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB028/kgogtv-328-42-7.solution) [328/**42**/7 Grimmy](https://i.imgur.com/Q0U1mxE.gif) | [99/99/**4** Unknown](https://cdn.discordapp.com/attachments/469290305292730368/795241992501067786/SPOILER_EXAPUNKS_-_KGOG-TV_99_99_4_2021-01-03-11-43-14.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB028/kgogtv-429-47-4.solution) [429/47/**4** Grimmy](https://i.imgur.com/UjQWYD5.gif)
|
| [Bank 2](https://zlbb.faendir.com/exa/PB025) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB025/equity-first-bank-42-73-10.solution) [**42**/73/10 Bacardi](https://i.imgur.com/g3veOON.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB025/equity-first-bank-1219-31-3.solution) [1219/**31**/3 Grimmy](https://i.imgur.com/DGfFJb2.mp4) | [56/70/**2** DarkMatter\_Zombie](https://imgur.com/XjBaANm)
|  | [**42**/75/7 PentaPig](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/BANK2/42%7C75%7C7) |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB025/equity-first-bank-1222-33-2.solution) [1222/33/**2** Grimmy](https://i.imgur.com/UQHd62U.mp4)
|
| [Modem 2](https://zlbb.faendir.com/exa/PB026B) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB026B/tec-exablaster-modem-469-100-42.solution) **469**/100/42 Grimmy | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB026B/tec-exablaster-modem-7187-22-753.solution) [7187/**22**/753 Grimmy](https://i.imgur.com/3CM048S.mp4) | [521/100/**9** Theophiphile](https://i.imgur.com/edgFc3h.gifv)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB026B/tec-exablaster-modem-7189-24-9.solution) [7189/24/**9** Grimmy](https://www.youtube.com/watch?v=koWhwc8XFyw)
|
| [Snaxnet 2](https://zlbb.faendir.com/exa/PB029B) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB029B/last-stop-snaxnet-47-97-55.solution) [**47**/97/55 Community](https://i.imgur.com/nlRij1b.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB029B/last-stop-snaxnet-9217-19-3253.solution) [9217/**19**/3253 Grimmy](https://www.youtube.com/watch?v=fnhFJidHmVc) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB029B/last-stop-snaxnet-62-83-7.solution) [62/83/**7** Grimmy](https://i.imgur.com/TyOu5e0.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB029B/last-stop-snaxnet-7213-24-7.solution) [7213/24/**7** Grimmy](https://i.imgur.com/ALYvguV.mp4)
|
| [Visual Cortex](https://zlbb.faendir.com/exa/PB030) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB030/mitsuzen-hdi10-211-75-14.solution) [**211**/75/14 Bacardi](https://i.imgur.com/hVdtBD6.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB030/mitsuzen-hdi10-993-19-300.solution) [993/**19**/300 Grimmy](https://i.imgur.com/dndN0XT.mp4?loop=1) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB030/mitsuzen-hdi10-217-59-10.solution) [217/59/**10** Grimmy](https://i.imgur.com/fJAzLX9.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB030/mitsuzen-hdi10-929-25-10.solution) [929/25/**10** Grimmy](https://i.imgur.com/Kd2oiZf.gif)
|
| [Holman](https://zlbb.faendir.com/exa/PB032) | [**359**/150/750 repoBoy](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/HOLMAN/359%7C150%7C750) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB032/holman-dynamics-23239-25-3.solution) [23239/**25**/3 Grimmy](https://i.imgur.com/Jh6IIsv.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB032/holman-dynamics-1170-150-3.solution) [1170/150/**3** Grimmy](https://i.imgur.com/PUxSd8b.mp4)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB032/holman-dynamics-23239-25-3.solution) [23239/25/**3** Grimmy](https://i.imgur.com/Jh6IIsv.mp4)
|
| [USGov](https://zlbb.faendir.com/exa/PB033) | [**233**/140/36 Bacardi](https://i.imgur.com/ORW9zgT.gif) | [38401/**39**/800 ivoryshard244](https://i.imgur.com/sTCpxaN.mp4) | [700/150/**6** Theophiphile](https://i.imgur.com/ny6YizP.mp4)
|  |  |  | [1509/114/**6** ac355deny](https://i.imgur.com/22t8HnY.gifv)
|
| [UN2](https://zlbb.faendir.com/exa/PB034) | [**55**/74/53 Bacardi](https://i.imgur.com/059HMRN.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB034/unknown-network-1808-15-3880.solution) 1808/**15**/3880 Grimmy | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB034/unknown-network-69-75-49.solution) [69/75/**49** Grimmy](https://i.imgur.com/MTw7Sbm.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB034/unknown-network-2270-19-49.solution) [2270/19/**49** Grimmy](https://i.imgur.com/bkignGn.gif)
|
| [Modem 3](https://zlbb.faendir.com/exa/PB035B) | [**148**/100/43 MoonsOfJupiter](https://i.imgur.com/w92UrMu.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB035B/tec-exablaster-modem-5341-29-185.solution) [5341/**29**/185 Grimmy](https://i.imgur.com/PWyb8ke.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB035B/tec-exablaster-modem-186-100-9.solution) [186/100/**9** Grimmy](https://i.imgur.com/KlMfNAB.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB035B/tec-exablaster-modem-5334-34-9.solution) [5334/34/**9** Grimmy](https://www.youtube.com/watch?v=PW2HGZ8w4vk)
|
| [Cerebral Cortex](https://zlbb.faendir.com/exa/PB036) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB036/mitsuzen-hdi10-130-149-131.solution) **130**/149/131 Bacardi | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB036/mitsuzen-hdi10-13961-29-224.solution) [13961/**29**/224 Grimmy](https://i.imgur.com/AshdLtN.mp4) | [247/149/**16** \_oddball\_](https://imgur.com/Cf34L2h)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB036/mitsuzen-hdi10-2295-46-16.solution) [2295/46/**16** Grimmy](https://i.imgur.com/GI1IxMX.gif)

### Bonus Puzzles

| Level | Cycles | Size | Activity
| ---  | ---  | --- | ---
| [mutex8021](https://zlbb.faendir.com/exa/PB054) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB054/bloodlust-online-83-148-50.solution) [**83**/148/50 Bacardi](https://i.imgur.com/XxBt6Sl.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB054/bloodlust-online-540-42-383.solution) [540/**42**/383 Grimmy](https://i.imgur.com/SQcJTHZ.mp4?loop=1) | [98/142/**11** DarkMatter\_Zombie](https://i.imgur.com/fmd9rTW.gifv)
|  |  |  | 354/85/**11** \_rice
|
| [NthDimension](https://zlbb.faendir.com/exa/PB053) | [**127**/100/39 Theophiphile](https://i.imgur.com/imBIKlg.gifv) | [725/**35**/97 prh75](https://imgur.com/HmUrfDW) | [143/95/**7** Theophiphile](https://i.imgur.com/c3ZHi6S.gif)
|  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB053/motor-vehicle-administration-882-35-92.solution) [882/**35**/92 Grimmy](https://i.imgur.com/kwrkjdj.gif) | [2109/49/**7** \_rice](https://i.imgur.com/o4Il0L2.gifv)
|
| [Ghast](https://zlbb.faendir.com/exa/PB050) | [**122**/148/34 repoBoy](https://i.imgur.com/cj8YF5w.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB050/cybermyth-studios-9671-68-14.solution) [9671/**68**/14 Grimmy](https://www.youtube.com/watch?v=DrsAaBDxdI8) | [161/145/**4** Theophiphile](https://i.imgur.com/aT3Cl9N.gifv)
|  |  |  | [887/106/**4** mplain](https://i.imgur.com/bEOwU81.gifv)
|
| [hydroponix](https://zlbb.faendir.com/exa/PB056) | [**274**/148/123 Grimmy](https://i.imgur.com/D7oSNFR) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB056/u.s.-department-of-defense-79136-39-4620.solution) [79136/**39**/4620 Grimmy](https://youtu.be/pW6i2a7eOh8) | [525/147/**38** Unknown](https://i.imgur.com/FWSd1u5.mp4)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB056/u.s.-department-of-defense-7451-130-38.solution) 7451/130/**38** Grimmy
|
| [=plastered](https://zlbb.faendir.com/exa/PB051) | [**5176**/150/2497 Bacardi](https://i.imgur.com/ICSv80p.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB051/tec-exablaster-modem-105006-39-11069.solution) 105006/**39**/11069 Grimmy | [27084/135/**1** Theophiphile](https://www.youtube.com/watch?v=3MTyiCakDf4)
|  |  |  | [114984/43/**1** repoBoy](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/MODEM4/114984%7C43%7C1)
|
| [selenium_wolf](https://zlbb.faendir.com/exa/PB057) | **376**/100/108 Bacardi | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB057/española-valley-high-school-905-39-15.solution) [905/**39**/15 Grimmy](https://i.imgur.com/8Zk2sAJ.gif) | [608/94/**3** Theophiphile](https://i.imgur.com/znt9N9u.gifv)
|  |  |  | [932/44/**3** PentaPig](https://i.imgur.com/QZVhggr.gifv)
|
| [x10x10x](https://zlbb.faendir.com/exa/PB052) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB052/mitsuzen-d300n-1012-150-37.solution) [**1012**/150/37 Bacardi](https://i.imgur.com/gPLbXzB.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB052/mitsuzen-d300n-39652-35-20275.solution) [39652/**35**/20275 Grimmy](https://www.youtube.com/watch?v=w5Qo2O0hdpI) | [5365/147/**4** repoBoy](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/x10x10x/5365%7C147%7C4)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB052/mitsuzen-d300n-8710-58-4.solution) [8710/58/**4** Grimmy](https://www.youtube.com/watch?v=pvqb_ulPr7E)
|
| [deadlock](https://zlbb.faendir.com/exa/PB055) | [**134**/143/47 \_oddball\_](https://imgur.com/6bv3Ld3) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB055/crystalair-international-13302-50-14.solution) [13302/**50**/14 Grimmy](https://i.imgur.com/LGb81OY.gifv) | [577/148/**6** Theophiphile](https://i.imgur.com/fSk3dyK.gifv)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB055/crystalair-international-4178-117-6.solution) 4178/117/**6** darkgiggs/Kat
|
| [Moss](https://zlbb.faendir.com/exa/PB058) | [**640**/100/51 Bacardi](https://i.imgur.com/UktExCH.gif) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB058/archlinux-3592-44-27.solution) [3592/**44**/27 Grimmy](https://i.imgur.com/RCkGhR0.mp4) | [872/100/**11** Theophiphile](https://i.imgur.com/gmKxuMd.gifv)
|  |  |  | [4443/45/**11** Moss](https://imgur.com/gtsllty.gifv)

### Cheesy solutions

| Level | Cycles | Size | Activity
| ---  | ---  | --- | ---
| [Pizza](https://zlbb.faendir.com/exa/PB003B?visualizerFilterExa-PB003B.modifiers.cheesy=true) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-12-18-2-c.solution) **12**/18/2/c Grimmy |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-12-19-1-c.solution) 12/19/**1**/c Grimmy
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB003B/euclids-pizza-12-19-1-c.solution) **12**/19/1/c Grimmy |  |
|
| [Snaxnet 1](https://zlbb.faendir.com/exa/PB005?visualizerFilterExa-PB005.modifiers.cheesy=true) | [**23**/48/3/c repoBoy](https://github.com/ExapunksBacardi/ExapunksRecords/tree/master/SNAXNET1/23%7C48%7C3%7Cc) |  | 24/50/**2**/c Unknown
|
| [Zebros](https://zlbb.faendir.com/exa/PB006B?visualizerFilterExa-PB006B.modifiers.cheesy=true) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB006B/zebros-copies-52-47-5-c.solution) **52**/47/5/c Grimmy |  |
|  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB006B/zebros-copies-52-49-4-c.solution) **52**/49/4/c Grimmy |  |
|
| [APL](https://zlbb.faendir.com/exa/PB021?visualizerFilterExa-PB021.modifiers.cheesy=true) |  |  | 33/70/**19**/c Snowball
|
| [KRO](https://zlbb.faendir.com/exa/PB024?visualizerFilterExa-PB024.modifiers.cheesy=true) |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB024/kings-ransom-online-136-100-24-c.solution) 136/100/**24**/c Bacardi
|
| [Holman](https://zlbb.faendir.com/exa/PB032?visualizerFilterExa-PB032.modifiers.cheesy=true) | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB032/holman-dynamics-86-149-3-c.solution) [**86**/149/3/c Bacardi](https://cdn.discordapp.com/attachments/469290305292730368/969131688850522142/EXAPUNKS_-_Holman_Dynamics_86_149_3_2022-04-28-09-02-22.gif) |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/MAIN_CAMPAIGN/PB032/holman-dynamics-86-149-3-c.solution) [86/149/**3**/c Bacardi](https://cdn.discordapp.com/attachments/469290305292730368/969131688850522142/EXAPUNKS_-_Holman_Dynamics_86_149_3_2022-04-28-09-02-22.gif)
|
| [Ghast](https://zlbb.faendir.com/exa/PB050?visualizerFilterExa-PB050.modifiers.cheesy=true) | **120**/150/19/c Bacardi |  |
|
| [hydroponix](https://zlbb.faendir.com/exa/PB056?visualizerFilterExa-PB056.modifiers.cheesy=true) |  |  | [📄](https://raw.githubusercontent.com/12345ieee/exapunks-leaderboard/master/BONUS_PUZZLES/PB056/u.s.-department-of-defense-7373-143-34-c.solution) [7373/143/**34**/c Grimmy](https://i.imgur.com/G6Y75zy.mp4)
|
