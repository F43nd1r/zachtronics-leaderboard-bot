/*
 * Copyright (c) 2026
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {Card, CardContent, CardHeader, CardMedia, styled, useTheme} from "@mui/material"
import heightExplanation from "./height_explanation.jpg"
import widthExplanation from "./width_explanation.jpg"
import boundingHexagonExplanation from "./bounding_hexagon_explanation.jpg"
import overlapExplanation from "./overlap_tutorial.mp4"
import {LinkOutlined} from "@mui/icons-material"
import {ReactNode, useMemo, useState} from "react"
import {HashLink} from "react-router-hash-link"
import {useLocation} from "react-router-dom"
import {Masonry} from "@mui/lab"

export default function HelpView() {
    document.title = "Help - Opus Magnum Leaderboards"
    return (
        <Masonry spacing={3} columns={{ xs: 1, sm: 2, md: 2, lg: 2, xl: 3, xxl: 3, xxxl: 4 }}>
            <HelpCard id="cost" title="Cost (g)" description={<>Cost of all parts in the solution, as defined by the game.</>} />
            <HelpCard id="cycles" title="Cycles (c)" description={<>Cycles needed to run the solution to completion, as defined by the game.</>} />
            <HelpCard
                id="area"
                title="Area (a)"
                description={
                    <>
                        Area used by the solution when running to completion, as defined by the game.
                        <p>Production puzzles don't show this value in game, but it is computed in the same way. The walls always count as area, so all solutions have a big number than it seems like they should.</p>
                        <p>If a solution has infinitely growing chains (whether polymer or waste), a<HL to="measure" text="@∞" /> can be expressed as a' or a''. a' is the average amount the area increases per output in the steady state. a'' is used when increasingly long chains get swung/rotated in the steady state, and is normalized such that a chain that grows 1 atom longer every output which swings across 60 degrees has a''=1.</p>
                    </>
                }
            />
            <HelpCard
                id="instructions"
                title="Instructions (i)"
                description={
                    <>
                        Instructions on all arms, as defined by the game.
                        <p>Repeat and return instructions are expanded to their actual contained instructions. The wait and halt instructions are not counted.</p>
                        <p>Non-production puzzles don't show this value in game, but it is computed in the same way.</p>
                    </>
                }
            />
            <HelpCard
                id="height"
                title="Height (h)"
                description={
                    <>
                        Number of rows in the area footprint of the solution.
                        <p>Height is measured on all three axes, and the smallest value is taken. This means rotation of the solution doesn't matter.</p>
                    </>
                }
                image={{ location: heightExplanation, alt: "Height Explanation" }}
            />
            <HelpCard
                id="width"
                title="Width (w)"
                description={
                    <>
                        Number of columns in the area footprint of the solution. Note that half hexes are possible in this metric (see image).
                        <p>Width is measured on all three axes, and the smallest value is taken, same as height. This is often used to improve the aspect ratio of width gifs.</p>
                        <p>Width is not measured on polymer puzzles.</p>
                    </>
                }
                image={{ location: widthExplanation, alt: "Width Explanation" }}
            />
            <HelpCard
                id="boundinghexagon"
                title="Bounding Hexagon (b)"
                description={
                    <>
                        Side length of the smallest regular hexagon that can fully contain the area footprint of the solution. Also commonly called Bestagon.
                        <p>Bounding hexagon is not measured on polymer puzzles.</p>
                    </>
                }
                image={{ location: boundingHexagonExplanation, alt: "Bounding Hexagon Explanation" }}
            />
            <HelpCard id="trackless" title="Trackless (T)"
                description={<>Solutions containing no tracks are classified as trackless.</>}
            />
            <HelpCard
                id="rate"
                title="Rate (r)"
                description={
                    <>
                        Average cycles between outputs in the steady state. A solution which does not run indefinitely has no rate.
                        <p>For example, a solution which outputs 3 times every 49 cycles repeatedly would have a rate of 49/3, no matter what the spacing of those outputs is. (See <HL to="measure" text="Measure Points" /> for more detail.)</p>
                        <p>This is often referred to as throughput, but is actually equal to 1/throughput.</p>
                        <p>The value is rounded up to 2 decimal places, so 10/3 is treated as the same rate as 3.34. This is intended to reduce the impact of "epsilon issues" where a solve can be modified to have rate arbitrarily close to some value by adding additional repetitive instructions or parts.</p>
                    </>
                }
            />
            <HelpCard
                id="overlap"
                title="Overlap (O)"
                description={
                    <>
                        Solutions containing overlapped parts are classified as Overlap.
                        <p>
                            The rules of the game would imply that components (inputs, outputs, glyphs, arms) cannot overlap with one another. However, this restriction is not enforced by the solution
                            file, and solutions which violate it can still be run and recorded in game.
                        </p>
                        <p>
                            Solutions with this behavior can be created using an external editor, or you can rotate an object, drag it off screen, shift click another object, and drag it in place, to
                            achieve overlap within the game itself as shown in the video.
                        </p>
                        <p>
                            When running solutions with this exploit, it is <b>highly encouraged to enable Hermit Mode</b> in the options, so as not to overwrite your existing scores.
                        </p>
                        <p>When solution scores are displayed on the leaderboard or adjacent tools, an O indicates that it does have overlap while a lack of O indicates that it does not. Having overlap makes a solution considered worse than one that doesn't (all else equal).</p>
                    </>
                }
                image={{
                    location: overlapExplanation,
                    alt: "Overlap Explanation",
                }}
            />
            <HelpCard
                id="illegal"
                title="General Solution Requirements"
                description={
                    <>
                        Through editing the solution file, it is possible to create aspects of the solution which are impossible to create otherwise. Some other restrictions also exist to prevent degenerate solutions involving numeric precision errors, and some are implemented due to community interest. Solutions which do any of the following are not accepted on the leaderboard:
                        <ul>
                            <li> In production puzzles, having parts or arm grabbers not inside a chamber, or breaking isolation mode. </li>
                            <li> Adding conduits that are not part of the puzzle, or removing or moving to the wrong chamber for ones that are part of the puzzle. </li>
                            <li> Having more than 16384 <HL to="instructions" text="instructions" />. </li>
                            <li> Placing parts too far away (16384 tiles) from the origin (the hex with the "critelli" symbol). (The allowed area is a hexagon.) </li>
                            <li> Using parts or instructions that are not enabled in the puzzle. </li>
                            <li> Placing duplicate (by ID) inputs or outputs. </li>
                            <li> Placing more than one berlo/disposal/proliferation/ravari. </li>
                            <li> Having gaps in track. (That is, having the next track hex within a track not be in one of the six tiles adjacent to the previous.) </li>
                        </ul>
                        <p>If you are playing normally, you should be able to ignore all of these.</p>
                    </>
                }
            />
            <HelpCard id="looping" title="Looping (L), Steady State" description={<>
                The leaderboard contains a simulator for the game which checks for the solution returning to a past state. If everything in the solution (instructions, arms, atoms) returns to the same state it was in at a previous point in time, the steady state is entered.
                <p>Infinitely growing chains, such as polymers, are allowed in the steady state as long as the growth is predictable and the same in every loop. For example, creating conditional wasteballs, where an action is performed with an increasingly long gap between each time, is not a steady state.</p>
                <p>For polymer output, additional outputs past 6 count the same way as the ones before 6 normally do, just extended farther to the right.</p>
                <p>Solutions which output (to each output) in the steady state are considered Looping. This is only used on the @V <HL to="manifold" text="manifolds" />, and indicates that a clean GIF of the solution (which outputs in the GIF) is possible to create.</p>
                <p>Note that, even if a solution is Looping, it is sometimes still worth using different GIF cycle bounds in order to show the setup portion of a solution.</p>
            </>} />
            <HelpCard
                id="measure"
                title="Measure Points"
                description={
                    <>
                        Certain metrics (<HL to="cycles" text="c" />/<HL to="rate" text="r" />, <HL to="area" text="a" />, <HL to="boundinghexagon" text="b" />, <HL to="height" text="h" />, <HL to="width" text="w" />) are evaluated at different measure points, which influences how long the solution must run before the value is checked.
                        <p>@V means the value at victory. At the point the final output is produced and the solution is solved, the value is checked and everything beyond that point is ignored.</p>
                        <p>@∞ means the value at infinity. This is only measured for <HL to="looping" text="Looping" /> solutions, and measures the value at the end of the steady state.</p>
                        <p>b@∞, h@∞, and w@∞ can have a score of ∞, but a@∞ has more precision if it would be infinite (see <HL to="area" text="its entry" />).</p>
                        <p>Some metrics, like r and a', depend on the number of outputs that occur in the steady state loop; this is measured as an extrapolation of what counts for victory for @V puzzles, taking the amount of full output sets that are performed in the loop. For polymer puzzles with an output multiplier, such as Electrum Separation, one monomer added to the polymer counts as multiple outputs, to maintain the ratio required to reach victory.</p>
                        <p><HL to="cycles" text="Cycles" /> is only measured @V and <HL to="rate" text="rate" /> is only measured @∞. Rate can be seen as c', given a different name due to common usage.</p>
                    </>
                }
            />
            <HelpCard
                id="manifold"
                title="Manifolds"
                description={
                    <>
                        Metrics are measured on several different manifolds, which each measure different metrics.
                        <p><HL to="area" text="a" />/<HL to="boundinghexagon" text="b" />/<HL to="height" text="h" />/<HL to="width" text="w" />: This determines which of the four arealike metrics are measured in this manifold. The other three are excluded. In production puzzles, these do not exist, instead having only the single manifold i.</p>
                        <p>V/∞: This determines the <HL to="measure" text="measure point" /> of the manifold. Only the metrics in the specified measure point are included in the manifold, with the exception of Looping being in @V. The metrics that do not have a metric point are included in both. A solution that is not Looping is not included in @∞ manifolds.</p>
                        <p>
                            For example, the manifold @bV measures g, c, i, b@V, L, T, and O. The main consequence of this system is that solutions that optimize metric combinations that never exist in the same manifold (such as c and r, or a@V and h@V) are not included on the leaderboard.
                        </p>
                    </>
                }
            />
            <HelpCard
                id="computed"
                title="Computed Metrics (Sum, Sum4, X)"
                description={
                    <>
                        Some metrics are computed based on the values of several other metrics, to be used in categories.
                        <p>Sum and Sum4 refer to <HL to="gold" text="g" />+<HL to="cycles" text="c" />+<HL to="area" text="a" /> and g+c+a+i respectively. In production puzzles, Sum is instead g+c+<HL to="instructions" text="i" />.</p>
                        <p>X means product. In the @V <HL to="measure" text="measure point" />, it is g*c*a, or g*c*i in production. In the @∞ measure point, it is <HL to="rate" text="r" />*g*i.</p>
                    </>
                }
            />
            <HelpCard
                id="categories"
                title="Categories"
                description={
                    <>
                        A category name contains several letters in a row. The first letter is the primary metric that is trying to be minimized; the second letter is used as a tiebreaker, and the third is a tiebreaker for that one, and so on.
                        <p>Each category belongs in a specific <HL to="manifold" text="manifold" />, although they may still use solutions that are only outside that manifold. More important is the <HL to="measure" text="measure point" /> (determining whether <HL to="looping" text="L" /> is required for a solution or not), which can usually be determined by whether the solution uses <HL to="cycles" text="C" /> or <HL to="rate" text="R" /> in its title (defaulting to @V when unclear). Note that TIA is @∞, and thus may have a higher instructions score than a solution in TIG, which is @V.</p>
                        <p>A full list of categories, grouped by manifold, can be found in the sidebar on the main page. Next to the category also specifies additional tiebreakers beyond the letters. (Aside from Overlap, these correspond exactly to the rule below.)</p>
                        <p>Beyond the specified letters for each category, the default tiebreaker order is <HL to="overlap" text="O" /><HL to="gold" text="G" />[CR][<HL to="area" text="A" /><HL to="boundinghexagon" text="B" /><HL to="height" text="H" /><HL to="width" text="W" />]L<HL to="instructions" text="I" /><HL to="trackless" text="T" />, or OG[CR]ILAT for production, only counting the metrics that are in the category's manifold. If there is a tie in the entire manifold, the additional tiebreaker is CAHWB@V then RAHWB@∞, although only solutions which are accepted on any <HL to="pareto" text="pareto frontier" /> are counted.</p>
                        <p>Overlap is treated specially, in that it is normally not allowed and the O in the category title indicates that it is allowed, as an overlap solution is considered worse than one that does not overlap. In tiebreakers, it follows the order specified in the category list (excluding the O) before O is checked. For example, OIC (@aV) specifies ICGA, so its full order is ICGA OLT HWB@V RAHWB@∞.</p>
                    </>
                }
            />
            <HelpCard
                id="pareto"
                title="Pareto Frontier"
                description={
                    <>
                        A solution is part of the pareto frontier of a puzzle if no other solution exists that is strictly better.
                        <p>A solution is strictly better than another if it is at least equal in all metrics, and better in at least one metric.</p>
                        <p>The pareto frontier is separate for each <HL to="manifold" text="manifold" />, although many solutions are in the pareto frontier for multiple. Only solutions that are on the pareto frontier of at least one manifold are accepted onto the leaderboard.</p>
                        <p>If a solution beats another in the pareto frontier of every metric ignoring manifolds, the solution is considered to be better than the other (removing the beaten record from the frontier). Otherwise, solutions are handled within the manifolds only.</p>
                        <p>If a solution ties (or worse) a solution on the pareto frontier in every manifold, the solution is rejected. (This biases toward earlier-submitted solutions.)</p>
                        <p>Mapping out the full pareto frontier for a puzzle is usually not possible, as an infinite amount of tradeoffs between metrics exist. However, Stabilized Water's pareto frontier is believed to be mostly complete.</p>
                    </>
                }
            />
            <HelpCard
                id="visualizer"
                title="Pareto Visualizer"
                description={
                    <>
                        To view what solutions exist on the <HL to="pareto" text="pareto frontier" />, a visualizer tool is available on the website.
                        <p>
                            Displaying the full pareto frontier is not possible, as each metric would require a separate axis, but the visualizer allows you to view and filter 2D or 3D projections of it.
                        </p>
                        <p>If you enable "Show only frontier" you can view the pareto frontier as it would look like if only the selected metrics existed.</p>
                        <p><HL to="area" text="a'" /> is counted as 100000 of that metric for the visualization, and a'' as 1e10. For example, a solution with 4/3 a' is shown as 133333a. If a metric is ∞, it does not appear on the visualizer set to that metric at all.</p>
                        <p>Note that the pareto visualizer allows selecting conflicting metrics which are never present in the same <HL to="manifold" text="manifold" />. If this is done, there may be gaps that a solution could fit into which are not accepted onto the leaderboard.</p>
                    </>
                }
            />
            <HelpCard
                id="submission"
                title="Submitting Solutions"
                description={
                    <>
                        To submit a solution, go to <a href="https://zlbb.faendir.com/upload">zlbb.faendir.com/upload</a> and input a name, solution file, and the gif of your solution.
                        <p>Names are not stored on the leaderboard, but the discord bot will still show your name.</p>
                        <p>See <a href="https://events.critelli.technology/static/where.html">this page</a> if you need to find your solution file directory.</p>
                        <p>To find the correct solution file, it may help to sort the folder by most recently updated. Changing anything (then undoing) is sufficient to modify a solution.</p>
                        <p>Sometimes the gif recorded by the game will improperly show the solution (e.g. doesn't loop and only shows a small part of the solve). In this case, press F10 to manually select the start and end cycle. If your solution is hard to see or record for some reason, you may also want to record or edit your gif in other ways, then use the <HL to="bot" text="discord bot" /> to submit.</p>
                        <p>If your solution is accepted on the leaderboard, the database will be modified, and you can find a log of the submission in a thread in the discord server. Otherwise, it will give an error that explains why the solution was not added.</p>
                        <p>The "Allow GIF Update" checkbox allows submitting solutions that exactly tie an existing solution in all metrics. This is intended for fixing broken GIFs.</p>
                        <p>Solutions can also be submitted using the <HL to="bot" text="discord bot" />, or <HL to="autosubmit" text="autosubmitted" /> by the game.</p>
                    </>
                }
            />
            <HelpCard
                id="bot"
                title="Discord Bot"
                description={
                    <>
                        The Discord Bot allows you to submit or query records. It can be found on the <a href="https://discord.gg/98QNzdJ">community discord server</a>, and is also receptive to DMs.
                        <p>
                            Use
                            <Code>/om submit solution:&lt;solution file&gt; gif-data:&lt;gif file&gt;</Code>
                            to submit your solution to the leaderboards.
                        </p>
                        <p>If you don't include a gif, you will be told what the result of submitting the solution would be. This is useful to know if you should record a gif in the first place.</p>
                        <p>You can substitute gif-data for gif-link, allowing inputting an externally hosted file instead of a gif. For very long solutions, it is often easier to submit a mp4 instead of a GIF.</p>
                        <p>Add <Code>allow-gif-update=True</Code> to allow submitting tied solutions, as on the <HL to="submission" text="website" />.</p>
                        <p>
                            To view <HL to="categories" text="category" /> records, use <Code>/om show puzzle:&lt;puzzle name&gt; category:&lt;short category name&gt;</Code>. This will display the selected record.
                        </p>
                        <p>
                            To browse records, use either <Code>/om list puzzle:&lt;puzzle name&gt;</Code> if you're looking for only category records, or <Code>/om frontier puzzle:&lt;puzzle name&gt;</Code> if you're looking for the full pareto frontier. It is generally recommended to do this exclusively in DMs as the message is public.
                        </p>
                        <p>A subset of the pareto frontier can be found by using <Code>/om query puzzle:&lt;puzzle name&gt; measure-point:&lt;VICTORY/INFINITY&gt; query:&lt;query&gt;</Code>. Queries work the same way as categories, but have no additional tiebreakers beyond the end of the query, simply showing every solution that's left. <HL to="computed" text="Computed metrics" /> are also usable here, and cross-<HL to="manifold" text="manifold" /> (but not across <HL to="measure" text="measure points" />) queries can be made, although submitting an improvement might not be accepted in that case.</p>
                        <p>(The discord bot has additional commands, but they are either for other Zachtronics game leaderboards or are privileged actions not available to the public.)</p>
                    </>
                }
            />
            <HelpCard
                id="autosubmit"
                title="Automatic Submission"
                description={
                    <>
                        On the Windows version, it is possible to make the game automatically submit your solutions to the leaderboard.
                        <p>In your <a href="https://events.critelli.technology/static/where.html">solutions folder</a>, there is a <Code>config.cfg</Code> file somewhere. Within this file, change the <Code>ExternalLeaderboardURLs = </Code> line to <Code>ExternalLeaderboardURLs = https://zlbb.faendir.com/om/game-api/YOUR_NAME</Code>, setting your name appropriately.</p>
                        <p>If you are not in Hermit Mode, a solution will be submitted once you finish recording a gif using the ingame gif recorder. Gifs with a large filesize (around 1000+ cycles in the gif) might not get submitted. GIF updates will not be submitted this way.</p>
                    </>
                }
            />
        </Masonry>
    )
}

interface MetricCardProps {
    id: string
    title: string
    description: ReactNode
    image?: {
        location: string
        alt: string
    }
}

function HelpCard(props: MetricCardProps) {
    const [linkVisibility, setLinkVisibility] = useState<"hidden" | "visible">("hidden")
    const theme = useTheme()
    const location = useLocation()
    const highlight = useMemo(() => (theme.palette.mode === "light" ? theme.palette.grey["400"] : theme.palette.grey["800"]), [theme.palette.mode, theme.palette.grey])
    return (
        <Card
            key={props.id}
            sx={{
                animation: location.hash === `#${props.id}` ? "fade 3s ease-in-out" : "none",
                "@keyframes fade": {
                    "0%": { backgroundColor: highlight },
                    "100%": { backgroundColor: "transparent" },
                },
            }}
            onMouseEnter={() => setLinkVisibility("visible")}
            onMouseLeave={() => setLinkVisibility("hidden")}
        >
            <div
                id={props.id}
                style={{
                    display: "block",
                    position: "relative",
                    top: `-${(theme.mixins.toolbar.minHeight as number) + 24}px`,
                    visibility: "hidden",
                }}
            />
            <CardHeader
                title={
                    <>
                        {props.title}
                        <HashLink smooth to={`#${props.id}`} style={{ visibility: linkVisibility, color: theme.palette.text.primary, padding: "0.5rem" }}>
                            <LinkOutlined fontSize={"small"} />
                        </HashLink>
                    </>
                }
            />
            {props.image && <CardMedia autoPlay loop muted component={props.image.location.endsWith(".mp4") ? "video" : "img"} src={props.image.location} alt={props.image.alt} />}
            <CardContent>{props.description}</CardContent>
        </Card>
    )
}

interface HLProps {
    to: string
    text: string
}
function HL(props: HLProps) {
    const theme = useTheme()
    return (
        <HashLink smooth to={`#${props.to}`} style={{ visibility: "visible", color: theme.palette.text.primary}}>{props.text}</HashLink>
    )
}

const Code = styled("code")`
    margin-left: 0.5em;
    margin-right: 0.5em;
`
