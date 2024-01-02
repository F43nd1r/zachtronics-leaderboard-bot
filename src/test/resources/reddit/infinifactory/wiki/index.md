## Infinifactory Leaderboard

Here are collected all the top scores to the official levels.

The leaderboard collects solution files of all the known top solutions.
For every primary metric (Cycles, Footprint, Blocks) both the other metrics are considered as tiebreakers,
so a level may have at most 2 lines in the table.  
There are additional categories for:

* best Footprint solutions without [placing blocks out of bound](https://i.imgur.com/VJW3LKD.mp4), a solution that builds OOB is marked as /O
* best Cycles solutions without the use of [Giant Rotating Arms](https://cdn.discordapp.com/attachments/281248543535267840/1038478497817038978/Infinifactory_2022.11.05_-_08.39.45.01_1.gif) (GRA for friends), aka "no rotation of objects made up of both factory and input blocks", a solution that uses GRA is marked as /G
* best Blocks solutions that run indefinitely, a solution that only makes a finite amount of outputs is marked as /F

Solutions are either attributed to the original author, or attributed to `Community` if many people obtained it
independently early in the game release period or many people collaborated on it.

There are many solves that lack precise score verification, they have `?` in place of some score parts and are marked with all the flags.

The 📄 icons are links to the solution files, which can be opened in-game.

#### Submitting a New Solution

To submit a solution you need to get the corresponding solution string in the save folder.

In the [Discord Server](https://discord.gg/98QNzdJ), invoke the Leaderboard Bot via
`/if submit solution:<solution link> author:<your name> score:<ccc/ff/bb[/OGF]>`.  
Use `m1` for the solution link to refer to the preceding message if that's how you uploaded the export.

The solution file is the part of the savefile which contains the necessary info, in this format:

    InputRate.1-1.1 = 1
    Solution.1-1.1 = AwAAAAAAAAA=

You can use the optional `videos` parameter to give videos of the solutions, separated by `,`.

#### Solution files repository

The Leaderboard Bot will cause each submitted solution export to be automatically rehosted
in the [leaderboard git repository](https://github.com/12345ieee/infinifactory-leaderboard),
where are stored solution files for the whole [pareto frontier](https://en.wikipedia.org/wiki/Pareto_front) of each level.

You can add frontier scores to the archive by going to the [Discord Server](https://discord.gg/98QNzdJ)
and invoking the Leaderboard Bot via `/if submit solution:<solution link> author:<your name> score:<ccc/ff/bb[/OGF]>`
exactly as you would with a record submission.

If you want to import a previously stored solution, you can find it by following the relevant link in this table (if it is a record solution);
or by going to the [Discord Server](https://discord.gg/98QNzdJ) and invoking the Leaderboard Bot via `/if frontier puzzle: <puzzle name>`.  
The files can be copied directly into the game's save folder and opened in-game.

### Proving Grounds

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Training Routine 1](https://zlbb.faendir.com/if/1-1) | [**24**/?/?/GF gtw123](https://imgur.com/a/hz3PR) | [**31**/?/?/F pseudonym404](https://i.imgur.com/jNUAr7m.gifv) | [?/**31**/?/OGF SirDredgery](https://www.reddit.com/r/infinifactory/comments/v3apk8/training_routine_1_in_31_footprint_with_clipping/) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-1/844-218-2.txt) [844/218/**2** snowball](https://i.imgur.com/uagjvke.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-1/73-37-43.txt) [73/**37**/43 Community](https://i.imgur.com/CRO13ME.gif) |
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-1/106-37-23.txt) [106/**37**/23 Community](https://i.imgur.com/SbQ8RnH.gif) |
|
| [Training Routine 2](https://zlbb.faendir.com/if/1-2) | [**27**/?/?/GF grindmastaflash](https://imgur.com/a/AThtE) | [**35**/?/? wl](https://i.imgur.com/XQ4YmZw.gifv) | [392/**21**/86/OF SirDredgery](https://www.reddit.com/r/infinifactory/comments/v0mg5e/training_routine_2_in_21_footprint_with_clipping/) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-2/84-632-3-F.txt) [84/632/**3**/F snowball](https://i.imgur.com/64a982u.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-2/198-24-85.txt) [198/**24**/85 Entity_](https://i.imgur.com/c0nqMid.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-2/104-61-4.txt) [104/61/**4** Community](https://i.imgur.com/HNQC3tD.gif)
|  |  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-2/161-53-4.txt) [161/53/**4** Community](https://i.imgur.com/hBHFFeU.gif)
|
| [Training Routine 3](https://zlbb.faendir.com/if/1-3) | [**41**/?/?/GF grindmastaflash](https://i.imgur.com/MW7ldHS.gifv) [^[2]](https://i.imgur.com/k7xTSJc.gifv) | [**43**/?/? wl](https://i.imgur.com/ak7JRZb.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-3/50-15-28.txt) [50/**15**/28 rolamni/vpumeyyv](https://cdn.discordapp.com/attachments/281248543535267840/1190347429892083795/2023-12-29-19-34-48.gif?ex=65a1786e&is=658f036e&hm=73d69a11181baf7edd99ba66bb5895ba369b7ecee9a020b76444de7f80d9eb78&) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-3/156-266-2-F.txt) [156/266/**2**/F vpumeyyv](https://youtu.be/qi5oMvyCm20)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-3/74-15-4.txt) [74/**15**/4 Community](https://i.imgur.com/3kGgrix.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-3/256-21-3.txt) [256/21/**3** Community](https://i.imgur.com/0ZGvv6D.gif)
|
| [Training Routine 4](https://zlbb.faendir.com/if/1-4) | [**31**/?/?/GF grindmastaflash](https://i.imgur.com/BrAmreh.gifv) [^[2]](https://i.imgur.com/GOtwVm3.gifv) [^[3]](https://i.imgur.com/yXb5QAN.gifv) | [**32**/?/?/F pseudonym404](https://i.imgur.com/WLuU11V.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-4/44-12-6.txt) [44/**12**/6 Community](https://i.imgur.com/X8D00oL.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-4/42-13-3.txt) [42/13/**3** Community](https://i.imgur.com/7vHi6dY.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-4/62-12-3.txt) [62/**12**/3 Community](https://i.imgur.com/0H7IlY0.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-4/62-12-3.txt) [62/12/**3** Community](https://i.imgur.com/0H7IlY0.gif)
|
| [Training Routine 5](https://zlbb.faendir.com/if/1-5) | [**28**/?/?/GF gtw123](https://imgur.com/a/G5SgX) | [**38**/?/? wl](https://imgur.com/a/MWSgI) | [?/**27**/?/OGF SirDredgery](https://www.reddit.com/r/infinifactory/comments/v0nspj/training_routine_5_in_27_footprint_with_clipping/) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-5/241-287-3-F.txt) [241/287/**3**/F snowball](https://gfycat.com/CooperativeGivingAgama)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-5/77-30-45.txt) [77/**30**/45 vpumeyyv](https://cdn.discordapp.com/attachments/281248543535267840/1190805785312247871/2023-12-31-01-56-16.gif?ex=65a3234f&is=6590ae4f&hm=a691e341b84dc15b683ba7ed749d1cf9e9433165783e6fdf4b090b20ad75114e&) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-5/198-197-4.txt) [198/197/**4** snowball](https://i.imgur.com/GNVjknC.gif) [^[2]](https://i.imgur.com/DejLsGX.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_1/1-5/105-30-18.txt) [105/**30**/18 vpumeyyv/rolamni](https://i.imgur.com/jOwhNcl.gif) |

### Skydock 19

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Munitions Refill Type 2](https://zlbb.faendir.com/if/2-1) | [**19**/?/?/GF gtw123](https://imgur.com/a/fab46) | [**30**/?/?/F pseudonym404](https://i.imgur.com/OmJ3lvp.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-1/47-26-19.txt) [47/**26**/19 rolamni](https://i.imgur.com/kkYsKOH.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-1/37-526-1-F.txt) [37/526/**1**/F snowball](https://i.imgur.com/d0GJJGi.mp4)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-1/84-26-5.txt) [84/**26**/5 Community](https://i.imgur.com/95o3AIf.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-1/95-150-1-F.txt) [95/150/**1**/F snowball](https://i.imgur.com/nhuCXLo.gif)
|  |  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-1/115-215-2.txt) [115/215/**2** snowball](https://i.imgur.com/NczwUVB.gif)
|  |  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-1/354-70-2.txt) [354/70/**2** snowball](https://i.imgur.com/KSQHmMR.gif)
|
| [Munitions Refill Type 6](https://zlbb.faendir.com/if/2-2b) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-2b/46-595-340-G.txt) [**46**/595/340/G AapOpSokken](https://i.imgur.com/GIahQSc.gifv) | [**52**/?/?/F rikswift123](https://www.reddit.com/r/infinifactory/comments/v3d079/22_munitions_refill_type_6_in_52_cycles_no_gras/) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-2b/85-23-35.txt) [85/**23**/35 Community](https://i.imgur.com/JtchBmR.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-2b/100-533-1-F.txt) [100/533/**1**/F AapOpSokken & snowball](https://i.imgur.com/VsWDWF4.gif)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-2b/97-23-10.txt) [97/**23**/10 vpumeyyv](https://i.imgur.com/GxKtkYl.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-2b/161-51-2.txt) [161/51/**2** Community](https://i.imgur.com/P6RrDdi.gif)
|
| [Shuttle Propulsion Units](https://zlbb.faendir.com/if/2-4) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-4/73-650-800-G.txt) [**73**/650/800/G AapOpSokken](https://i.imgur.com/aXDr6j0.gifv) [^[2]](https://i.imgur.com/ORnf1fN.gifv) | [**78**/?/?/F rikswift123](https://www.reddit.com/r/infinifactory/comments/va4scj/23_shuttle_propulsion_units_in_78_cycles_no_gras/) | ?/**23**/?/GF Community | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-4/277-438-4-F.txt) [277/438/**4**/F snowball](https://i.imgur.com/b8aFxYn.gif)
|  |  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-4/380-226-4.txt) [380/226/**4** ravencoff](https://i.imgur.com/rrwW3b7.gif)
|
| [Wave Detection Array](https://zlbb.faendir.com/if/2-3) | [**56**/?/?/GF grindmastaflash](https://i.imgur.com/ORNeDKN.gifv) [^[2]](https://i.imgur.com/UwzXgdm.gifv) | [**64**/?/?/F rikswift123](https://imgur.com/gallery/BL4ZZDB) | ?/**40**/?/GF Community | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-3/1611-1394-4-F.txt) [1611/1394/**4**/F snowball](https://i.imgur.com/l0x7Gkz.gifv)
|  |  |  |  | [?/?/**5**/G Ravencoff](https://i.imgur.com/lPYTWPd.gifv)
|
| [Guided Javelin Type 1](https://zlbb.faendir.com/if/2-5) | [**54**/?/?/GF grindmastaflash](https://i.imgur.com/32XUL90.gifv) [^[2]](https://i.imgur.com/DXZD09x.gifv) [^[3]](https://i.imgur.com/lcshLSu.gifv) | [**58**/?/?/F rikswift123](https://imgur.com/gallery/rSAzf9w) | ?/**39**/?/GF Community | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_2/2-5/1212-1963-3-F.txt) [1212/1963/**3**/F snowball](https://i.imgur.com/U68wg1E.gifv)
|  |  |  |  | [?/?/**4** wl](https://i.imgur.com/JzEsjw4.gifv)

### Resource Site 526.81

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Optical Sensor Array Type 2](https://zlbb.faendir.com/if/3-1b) | [**48**/?/?/GF andy75381](https://imgur.com/6DSD0A7) | [**51**/?/?/F rikswift123](https://www.reddit.com/r/infinifactory/comments/lqmywh/31_optical_sensor_array_type_2_in_51_cycles_no/) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_3/3-1b/115-27-49.txt) [115/**27**/49 Community](https://i.imgur.com/HiYQMeZ.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_3/3-1b/450-49-5.txt) [450/49/**5** Ravencoff](https://i.imgur.com/Q72UJP8.gif)
|
| [Landing Alignment Lights](https://zlbb.faendir.com/if/3-2) | [**58**/?/?/GF DarkMatter_Zombie](https://www.youtube.com/watch?v=msSBI0getQk) | [**65**/?/?/F DarkMatter_Zombie](https://youtu.be/EFxkjaZarD8) | ?/**54**/?/GF Xavr0k | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_3/3-2/1213-868-6.txt) [1213/868/**6** snowball](https://jeremyg.nl/c/326-3.gif)
|
| [Optical Sensor Array Type 4](https://zlbb.faendir.com/if/3-3) | [**75**/407/200/G DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/1859dy8/new_top_cycle_33_optical_sensor_array_type_4_75/) | [**83**/?/?/F caeonosphere](https://i.imgur.com/wOMnDtv.gifv) | [?/**44**/?/GF Xavr0k](https://i.imgur.com/XBnBw3E.gif) | [?/?/**6**/G Ravencoff](https://i.imgur.com/8iXyvjX.gifv)
|
| [Small Excavator](https://zlbb.faendir.com/if/3-5b) | [**122**/?/?/GF DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/b7iv8o/34_small_excavator_122_cycles_gras/) | [**131**/?/?/F rikswift123](https://imgur.com/gallery/BdSYHq7) | [441/**44**/175/G Xavr0k](https://i.imgur.com/d719whr.jpeg) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_3/3-5b/8140-2723-7.txt) [8140/2723/**7** snowball](https://i.imgur.com/sFiqafY.gifv)
|
| [Cargo Uplifter](https://zlbb.faendir.com/if/3-4b) | [**82**/?/?/GF DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/qfp8jq/34_cargo_uplifter_82_cycles_gras/) | [**85**/?/?/F rikswift123](https://www.reddit.com/r/infinifactory/comments/sdezgl/35_cargo_uplifter_in_85_cycles_no_gras_new_record/?utm_source=reddit&utm_medium=usertext&utm_name=infinifactory&utm_content=t1_huc8u69) | ?/**53**/?/GF Community | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_3/3-4b/6295-4492-5.txt) [6295/4492/**5** snowball](https://i.imgur.com/hknp970.gifv)

### Production Zone 2

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Terminal Display Reclamation](https://zlbb.faendir.com/if/4-1) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-1/16-151-36-G.txt) [**16**/151/36/G AapOpSokken](https://i.imgur.com/eIkB2eW.gif) [^[2]](https://i.imgur.com/6QxzqB1.gif) | [**22**/?/?/F pseudonym404](https://i.imgur.com/qWNdwHI.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-1/50-23-21.txt) [50/**23**/21 Community](https://i.imgur.com/gmW0QTL.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-1/73-47-4-F.txt) [73/47/**4**/F Community](https://i.imgur.com/tdD36t4.gif)
|  |  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-1/47-49-5.txt) [47/49/**5** snowball](https://i.imgur.com/ZRUPPjw.gif)
|  |  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-1/103-33-5.txt) [103/33/**5** Entity_](https://i.imgur.com/sgXcy0Z.gif)
|
| [Shuttle Maintenance](https://zlbb.faendir.com/if/4-3) | [**76**/?/?/F gtw123](https://imgur.com/a/9zNHPFv) | [**76**/?/?/F gtw123](https://imgur.com/a/9zNHPFv) | ?/**79**/?/GF Community | [?/?/**10** wl](https://i.imgur.com/NIQEUQm.gifv)
|
| [Oversight Terminal Model 6](https://zlbb.faendir.com/if/4-2) | [**88**/?/?/F rikswift123](https://imgur.com/gallery/iagjJfk) | [**88**/?/?/F rikswift123](https://imgur.com/gallery/iagjJfk) | [?/**29**/199/OGF SirDredgery](https://www.reddit.com/r/infinifactory/comments/uz0w1z/new_record_oversight_terminal_model_6_in_29/) | [?/?/**8** wl](https://i.imgur.com/ngczTap.gifv)
|  |  |  | ?/**37**/?/GF Community |
|
| [Drone Maintenance](https://zlbb.faendir.com/if/4-4) | [**41**/?/?/GF gtw123](https://youtu.be/3G52OVHuVVc) | [**43**/?/?/F gtw123](https://imgur.com/a/4y5nrMf) | [95/**89**/177/G Xavr0k](https://i.imgur.com/Tv74rQs.jpeg) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-4/323-332-7.txt) [323/332/**7** snowball](https://jeremyg.nl/c/447-1.gif)
|
| [Furnished Studio Apartment](https://zlbb.faendir.com/if/4-5) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-5/69-448-374.txt) [**69**/448/374 ToughThought](https://i.imgur.com/QatUF02.gifv) [^[2]](https://i.imgur.com/1r4xXdC.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_4/4-5/69-448-374.txt) [**69**/448/374 ToughThought](https://i.imgur.com/QatUF02.gifv) [^[2]](https://i.imgur.com/1r4xXdC.gifv) | [?/**243**/? wl](https://i.imgur.com/oqkh8tU.gifv) | [?/?/**9** wl](https://i.imgur.com/KiaqRFn.gifv)

### Resource Site 338.11

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Guided Javelin Type 2](https://zlbb.faendir.com/if/5-1) | [**50**/?/?/GF gtw123](https://youtu.be/ZG4d96J3mR4) | [**58**/?/?/F rikswift123](https://imgur.com/gallery/T2EkGy7) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_5/5-1/129-32-31.txt) [129/**32**/31 Community](https://i.imgur.com/uT75JAf.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_5/5-1/392-3123-4-F.txt) [392/3123/**4**/F snowball](https://jeremyg.nl/c/2015-06-26-03-21-00.gif) [^[2]](https://jeremyg.nl/c/DmT4S.png)
|  |  |  | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_5/5-1/390-32-7.txt) [390/**32**/7 snowball](https://i.imgur.com/TEYmiJL.gif) | [?/?/**5** wl](https://i.imgur.com/J7VQHlG.gifv)
|
| [Gneiss Chair](https://zlbb.faendir.com/if/5-3) | [**136**/?/?/GF gtw123](https://imgur.com/a/6QuGa) | [**139**/?/?/F rikswift123](https://www.reddit.com/r/infinifactory/comments/t0juh4/52_gneiss_chair_in_139_cycles_no_gras_new_record/) | ?/**35**/?/GF Xavr0k | [?/?/**9**/G NohatCoder](https://i.imgur.com/iWIULCR.gifv)
|
| [Relay Satellite](https://zlbb.faendir.com/if/5-4b) | [**56**/?/?/GF grindmastaflash](https://i.imgur.com/eeCa73Q.gifv) [^[2]](https://i.imgur.com/KpF3WGg.gifv) [^[3]](https://i.imgur.com/A7o5WYr.gifv) [^[4]](https://i.imgur.com/y2gCOZd.gifv) | [**66**/?/?/F rikswift123](https://www.reddit.com/r/infinifactory/comments/losn7s/53_relay_satellite_in_66_cyles_no_gras/) | ?/**50**/?/GF Community | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_5/5-4b/932-2291-6.txt) [932/2291/**6** snowball](https://i.imgur.com/ESYf22n.gifv)
|
| [Terrestrial Surveyor (5-4)](https://zlbb.faendir.com/if/5-2b) | [**82**/304/586/F ToughThought](https://i.imgur.com/LPWo9Gp.gifv) [^[2]](https://i.imgur.com/uO3Iqce.gifv) | [**82**/304/586/F ToughThought](https://i.imgur.com/LPWo9Gp.gifv) [^[2]](https://i.imgur.com/uO3Iqce.gifv) | ?/**61**/?/GF Bidji29 | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_5/5-2b/507-280-15.txt) [507/280/**15** snowball](https://i.imgur.com/w0W14gl.mp4)
|
| [Anti-Javelin Point Defense](https://zlbb.faendir.com/if/5-5) | [**48**/?/?/GF gtw123](https://youtu.be/exaLS7Synkg) | [**56**/?/?/F gtw123](https://imgur.com/a/XZcZ7) | [?/**75**/?/GF Xavr0k](https://i.imgur.com/AaKh6PZ.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_5/5-5/3397-2907-8.txt) [3397/2907/**8** snowball](https://i.imgur.com/sB73TPi.gifv)

### Resource Site 902.42

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Meat Product Type 57](https://zlbb.faendir.com/if/6-1) | [**143**/?/?/F rikswift123](https://imgur.com/gallery/ZrCQQUe) | [**143**/?/?/F rikswift123](https://imgur.com/gallery/ZrCQQUe) | [?/**51**/?/GF Glaiel-Gamer](https://i.imgur.com/ZiLtkCg.mp4) | [?/?/**3** wl](https://i.imgur.com/YqDmTTZ.gifv)
|
| [Relaxant Formula 13](https://zlbb.faendir.com/if/6-2) | [**54**/?/?/GF gtw123](https://www.youtube.com/watch?v=PL9qXy5YY8c) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_6/6-2/77-540-449.txt) [**77**/540/449 ToughThought](http://i.imgur.com/GHDky5k.gifv) [^[2]](http://i.imgur.com/NqBvtGy.gifv) [^[3]](http://i.imgur.com/8adTYXB.gifv) | ?/**93**/?/GF Xavr0k | [?/?/**12**/G FancySpaceHorse](https://i.imgur.com/O74HHUD.gifv) [^[2]](https://i.imgur.com/hlQB2Zp.gifv)
|
| [Terrestrial Drone](https://zlbb.faendir.com/if/6-3) | [**125**/?/?/GF DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/q7it5n/63_terrestrial_drone_125_cycles_gras/) | [**126**/?/?/F DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/q72gan/63_terrestrial_drone_126_cycles_no_gras/) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_6/6-3/554-96-197.txt) [554/**96**/197 ToughThought](https://i.imgur.com/nuHentH.png) | [699/215/**23** SolidJim](https://www.youtube.com/watch?v=47v1FLy5Ohw)
|
| [Meat Product Type 101](https://zlbb.faendir.com/if/6-4) | [**48**/?/?/GF grindmastaflash](https://i.imgur.com/1JIpoKd.gifv) [^[2]](https://i.imgur.com/IOZ7AxQ.gifv) | [**54**/?/?/F grindmastaflash](https://i.imgur.com/HiVgNgd.gifv) [^[2]](https://i.imgur.com/lgOdinx.gifv) | ?/**130**/?/GF Lyshkami | [?/?/**8**/G Community](https://imgur.com/gallery/Yxm7diN)
|
| [Aerial Combat Shuttle](https://zlbb.faendir.com/if/6-5) | [**144**/?/?/GF gtw123](https://imgur.com/a/DgHOU) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_6/6-5/160-531-318.txt) [**160**/531/318 ToughThought](http://i.imgur.com/8eomMNa.gifv) [^[2]](http://i.imgur.com/e1yhuG0.gifv) [^[3]](http://i.imgur.com/sCOg8sr.gifv) [^[4]](http://i.imgur.com/o7qdnYH.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_6/6-5/1039-119-260.txt) [1039/**119**/260 ToughThought](https://i.imgur.com/A75jMrh.png) | [?/?/**32**/G caeonosphere](https://imgur.com/a/W5lUK)

### The Heist

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Teleporter Experiment 1](https://zlbb.faendir.com/if/7-1) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-1/37-602-693-G.txt) [**37**/602/693/G ToughThought](http://i.imgur.com/4AFsdmT.gifv) [^[2]](http://i.imgur.com/mxbAVf3.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-1/49-428-795.txt) [**49**/428/795 ToughThought](http://i.imgur.com/CZCve91.gifv) [^[2]](http://i.imgur.com/6eWuYZT.gifv) | ?/**30**/?/GF Community | [?/?/**5**/G Community](https://i.imgur.com/pDxojiK.gifv)
|
| [Teleporter Experiment 2](https://zlbb.faendir.com/if/7-2) | [**94**/?/?/GF Xavr0k](https://imgur.com/a/vjrtq) | [**105**/?/?/F gtw123](https://imgur.com/a/vjusk) | ?/**36**/?/GF Community | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-2/329-42-5.txt) [329/42/**5** Entity_](https://i.imgur.com/Y5DD8S8.gif) [^[2]](https://i.imgur.com/VgNfo70.mp4)
|
| [Teleporter Experiment 3](https://zlbb.faendir.com/if/7-3) | [**35**/?/?/GF gtw123](https://youtu.be/ALQfiN80FGU) | [**53**/?/?/F grindmastaflash](https://i.imgur.com/AeOPrlY.gifv) [^[2]](https://i.imgur.com/BpQQJIv.gifv) [^[3]](https://i.imgur.com/d4ZyS4Z.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-3/105-45-120.txt) [105/**45**/120 Community](https://i.imgur.com/2BGkkbi.gif) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-3/267-83-10.txt) [267/83/**10** Entity_](https://i.imgur.com/0FADqH0.mp4)
|
| [Teleporter Experiment 4](https://zlbb.faendir.com/if/7-4) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-4/95-1051-1059-G.txt) [**95**/1051/1059/G ToughThought](http://i.imgur.com/GxXAh2e.gifv) [^[2]](http://i.imgur.com/r57tA0v.gifv) | [**104**/?/?/F gtw123](https://imgur.com/a/Ycy2T) | ?/**51**/?/GF myugaru | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_7/7-4/1487-2872-7.txt) [1487/2872/**7** snowball](https://i.imgur.com/85242yg.mp4)
|
| [Terrestrial Surveyor (7-5)](https://zlbb.faendir.com/if/7-5) | [**116**/?/?/GF gtw123](https://imgur.com/a/uTVO1) | [**133**/?/?/F gtw123](https://imgur.com/a/ss3S0) | [?/**86**/?/GF SirDredgery](https://www.reddit.com/r/infinifactory/comments/snxeow/new_footprint_record_for_the_last_3_levels_of_the/) | [604/204/**18** SolidJim](https://imgur.com/a/RWqpy)
|
| [Anti-Javelin Satellite](https://zlbb.faendir.com/if/7-6) | [**165**/?/?/GF gtw123](https://imgur.com/a/3lsW2) | [**177**/?/?/F gtw123](https://imgur.com/a/7TiuW) | [?/**95**/?/GF SirDredgery](https://www.reddit.com/r/infinifactory/comments/snxeow/new_footprint_record_for_the_last_3_levels_of_the/) | [611/270/**23** SolidJim](https://youtu.be/5yBKi1uyBak)
|
| [The Big Blowout](https://zlbb.faendir.com/if/7-7) | [**227**/?/?/GF gtw123](https://imgur.com/a/HFcuXVA) | [**247**/?/?/F gtw123](https://imgur.com/a/Tc8Mk) | [?/**87**/?/GF SirDredgery](https://www.reddit.com/r/infinifactory/comments/snxeow/new_footprint_record_for_the_last_3_levels_of_the/) | [?/?/**24**/G Community](https://imgur.com/a/ZSz6j8z)

### Production Zone 1

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Hull Panels](https://zlbb.faendir.com/if/8-1) | [**92**/?/?/GF Entity_](https://i.imgur.com/nKevJyK.gifV) | [**95**/?/?/F Entity_](https://i.imgur.com/O4B2YEG.gifv) | [?/**39**/?/GF myugaru](https://i.imgur.com/8dgfIhf.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_8/8-1/590-593-4.txt) [590/593/**4** snowball](https://jeremyg.nl/c/814-3.gif)
|
| [Central Axis Support](https://zlbb.faendir.com/if/8-2) | [**67**/?/?/GF andy75381](https://imgur.com/ScIwxMv) [^[2]](https://imgur.com/ALJ5zss) [^[3]](https://imgur.com/h2suQqc) | [**73**/?/?/F gtw123](https://imgur.com/a/deIT6) | [2607/**77**/?/GF myugaru](https://steamuserimages-a.akamaihd.net/ugc/46503738257579284/2EB3871BA482EB9523E1D897D60EEFB81E7FB953/) | [?/?/**8**/G Community](https://i.imgur.com/QzvDJbK.gifv)
|
| [Docking Clamp](https://zlbb.faendir.com/if/8-6) | [**163**/?/?/GF andy75381](https://imgur.com/vPUHYoP) | [**173**/?/?/F DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/9izago/83_docking_clamp_173_cycles_no_gras/) | [?/**74**/?/GF myugaru](https://i.imgur.com/leS1L6p.mp4) | [?/?/**8**/G NohatCoder](https://i.imgur.com/GQtb0WT.gifv)
|
| [Structural Frame](https://zlbb.faendir.com/if/8-4) | [**161**/?/?/GF andy75381](https://imgur.com/NpKH4Dv) [^[2]](https://imgur.com/tulBOne) [^[3]](https://imgur.com/39adWP6) | [**172**/?/?/F andy75381](https://imgur.com/jL6ftDl) | [?/**66**/?/GF myugaru](https://i.imgur.com/Fov7BF6.mp4) | [?/?/**7**/G Community](https://i.imgur.com/uDlFBnt.gifv)
|
| [Anti-Javelin Battery](https://zlbb.faendir.com/if/8-5) | [**39**/?/?/GF gtw123](https://youtu.be/PwUYY0bWVw8) | [**43**/?/?/F gtw123](https://imgur.com/a/cu98X) | [809/**89**/?/GF myugaru](https://steamuserimages-a.akamaihd.net/ugc/46504330037593372/830F908766894F4B06570C49539C9C7EFE6A0601/) | [?/?/**15** wl](https://i.imgur.com/2ScbTDd.gifv)
|
| [Mag-Tube Corridor](https://zlbb.faendir.com/if/8-7) | [**251**/?/?/F grindmastaflash](https://i.imgur.com/SzAkFDP.gifv) [^[2]](https://i.imgur.com/KG9eqGz.gifv) [^[3]](https://i.imgur.com/v04aARL.gifv) | [**251**/?/?/F grindmastaflash](https://i.imgur.com/SzAkFDP.gifv) [^[2]](https://i.imgur.com/KG9eqGz.gifv) [^[3]](https://i.imgur.com/v04aARL.gifv) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_8/8-7/6299-97-463.txt) [6299/**97**/463 Tiavor](https://i.imgur.com/BR5bqdg.mp4) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_8/8-7/6104-991-17.txt) [6104/991/**17** snowball](https://i.imgur.com/x9rQMHB.mp4)
|
| [Solar Cell Array](https://zlbb.faendir.com/if/8-3) | [**75**/?/?/GF gtw123](https://youtu.be/-sRyeUq1Hvg) | [**126**/?/?/F gtw123](https://imgur.com/a/ULARhQI) | ?/**64**/?/GF Xavr0k | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_8/8-3/1551-566-13.txt) [1551/566/**13** snowball](https://i.imgur.com/kOz3Lkg.gifv)

### Atropos Station

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Landing Alignment Guide](https://zlbb.faendir.com/if/9-1) | [**64**/?/?/GF gtw123](https://i.imgur.com/TzPCXfa.gifv) | [**66**/?/?/F gtw123](https://imgur.com/a/9lsUw) | [?/**55**/410/G Xavr0k](https://i.imgur.com/Lh73hoa.jpeg) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_9/9-1/548-542-4.txt) [548/542/**4** snowball](https://jeremyg.nl/c/914-1.gif)
|
| [Control Console](https://zlbb.faendir.com/if/9-6) | [**45**/?/?/GF DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/eewo07/92_control_console_45_cycles_gras/) | [**88**/?/?/F DarkMatter_Zombie](https://youtu.be/caEqdtYlQvE) | [867/**98**/200 SolidJim](https://www.youtube.com/watch?v=fwvr0u1voeg) | [?/?/**15** wl](https://i.imgur.com/B3EEzhO.gifv)
|
| [Laser Calibration Target](https://zlbb.faendir.com/if/9-4) | [**115**/?/?/GF DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/eqzqpi/93_laser_calibration_target_115_cycles_gras/) | [**119**/?/?/F DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/eqzq1s/93_laser_calibration_target_119_cycles_no_gras/) | [?/**55**/444/G Xavr0k](https://i.imgur.com/5Nwi784.jpeg) | [?/?/**4**/G NohatCoder](https://i.imgur.com/cDzRnNO.gifv)
|
| [Life Support System](https://zlbb.faendir.com/if/9-3) | [**39**/?/?/GF gtw123](https://imgur.com/a/qodsp) | [**51**/?/?/F gtw123](https://imgur.com/a/hciSp) | [?/**89**/347/G Xavr0k](https://i.imgur.com/4irI5wF.jpeg) | [?/?/**8**/G Community](https://i.imgur.com/eGMJKPJ.gifv)
|
| [Space Buoy](https://zlbb.faendir.com/if/9-2) | [**86**/?/?/GF gtw123](https://imgur.com/a/mQgOe) | [**97**/?/?/F gtw123](https://imgur.com/a/7Y8c7) | [?/**78**/?/GF Community](https://imgur.com/a/MStMtCK) | [📄](https://raw.githubusercontent.com/12345ieee/infinifactory-leaderboard/master/ZONE_9/9-2/836-675-8.txt) [836/675/**8** snowball](https://i.imgur.com/5yfYeSE.mp4)
|
| [Resistance Shuttles](https://zlbb.faendir.com/if/9-7) | [**95**/?/?/GF gtw123](https://imgur.com/a/iTA1X) | [**97**/?/?/F gtw123](https://imgur.com/a/VwXst) | [1753/**138**/243 SolidJim](https://www.youtube.com/watch?v=WYM9BnvVIbg) | [1034/422/**50**/F SolidJim](https://youtu.be/ih6kwGz4v3c)
|  |  |  |  | [1034/361/**52** SolidJim](https://i.imgur.com/tFC91T0.mp4)

### The Homeward Fleet

| Name | Cycles | Cycles (No GRAs) | Footprint | Blocks
| ---  | ---  | --- | --- | ---
| [Navigation Computer](https://zlbb.faendir.com/if/10-2) | [**84**/?/?/GF DarkMatter_Zombie](https://youtu.be/AaQdrTw3pug) | [**87**/?/?/F DarkMatter_Zombie](https://youtu.be/D0Z-s70qwr8) | [?/**158**/?/GF Community](https://imgur.com/a/h6n4SUn) | [?/?/**25**/G OracleofEpirus](https://i.imgur.com/xkiPeLl.gifv)
|
| [Fusion Reactor](https://zlbb.faendir.com/if/10-3) | [**498**/?/?/GF gtw123](https://youtu.be/AZloBE1Sv1Y) | [**503**/?/?/F gtw123](https://www.youtube.com/watch?v=BSHR0w1sveQ) | [?/**120**/999/G SirDredgery](https://imgur.com/a/WM90hlC) | [?/?/**54**/G Community](https://gfycat.com/ImmediateSpectacularAnteater)
|
| [Crew Quarters](https://zlbb.faendir.com/if/10-1) | [**145**/?/?/GF DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/gu4g47/103_crew_quarters_145_cycles_gras/) | [**155**/?/?/F DarkMatter_Zombie](https://youtu.be/CGv78i9sewg) | [?/**250**/?/GF Community](https://imgur.com/a/WMB2FMs) | [1119/921/**45** SolidJim](https://youtu.be/uv4teRMoQ0k)
|
| [Plasma Engine](https://zlbb.faendir.com/if/10-4) | [**619**/?/?/F gtw123](https://www.youtube.com/watch?v=PrgZu5pfFPI&feature=youtu.be) | [**619**/?/?/F gtw123](https://www.youtube.com/watch?v=PrgZu5pfFPI&feature=youtu.be) | [?/**273**/830/G SirDredgery](https://i.imgur.com/QdfMYZl.png) | [?/?/**49**/G OracleofEpirus](https://i.imgur.com/CXizOfM.jpg)
|
| [Skip Drive](https://zlbb.faendir.com/if/10-6) | [**129**/?/?/F DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/hly4df/106_skipdrive_129_cycles_no_gras/) | [**129**/?/?/F DarkMatter_Zombie](https://www.reddit.com/r/infinifactory/comments/hly4df/106_skipdrive_129_cycles_no_gras/) | [589/**292**/324 SolidJim](https://www.youtube.com/watch?v=pZ8mR7ejHdo) | [?/?/**40**/G Community](https://imgur.com/a/Rgasm)