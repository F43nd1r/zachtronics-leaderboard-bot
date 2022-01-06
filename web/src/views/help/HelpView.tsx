/*
 * Copyright (c) 2022
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

import { Card, CardContent, CardHeader, CardMedia, Grid, useTheme } from "@mui/material"
import heightExplanation from "./height_explanation.jpg"
import widthExplanation from "./width_explanation.jpg"
import overlapExplanation from "./overlap_tutorial.mp4"
import { LinkOutlined } from "@mui/icons-material"
import { useState } from "react"
import { HashLink } from "react-router-hash-link"

export default function HelpView() {
    document.title = "Help - Opus Magnum Leaderboards"
    return (
        <Grid container spacing={3} alignItems="stretch" sx={{ paddingBottom: 2 }}>
            <MetricCard id="cost" title="Cost (g)" description={<>Cost of all parts in the solution, as defined by the game.</>} />
            <MetricCard id="cycles" title="Cycles (c)" description={<>Cycles needed to run the solution to completion, as defined by the game.</>} />
            <MetricCard
                id="area"
                title="Area (a)"
                description={
                    <>
                        Area used by the solution when running to completion, as defined by the game.
                        <p>Production puzzles don't show this value in game, but it is computed in the same way.</p>
                    </>
                }
            />
            <MetricCard
                id="instructions"
                title="Instructions (i)"
                description={
                    <>
                        Instructions on all arms, as defined by the game.
                        <p>Repeat and return instructions are expanded to their actual contained instructions. The wait instruction is not counted.</p>
                        <p>Non-production puzzles don't show this value in game, but it is computed in the same way.</p>
                    </>
                }
            />
            <MetricCard id="sum" title="Sum (g+c+a / g+c+i)" description={<>Sum of cost, cycles and area (or instructions in production puzzles).</>} />
            <MetricCard id="sum4" title="Sum4 (g+c+a+i)" description={<>Sum of cost, cycles, area and instructions.</>} />
            <MetricCard
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
            <MetricCard
                id="width"
                title="Width (w)"
                description={
                    <>
                        Number of columns in the area footprint of the solution. Note that half hexes are possible in this metric (see image).
                        <p>Width is measured on all three axes, and the smallest value is taken, same as height. This is often used to improve the aspect ratio of width gifs.</p>
                    </>
                }
                image={{ location: widthExplanation, alt: "Width Explanation" }}
            />
            <MetricCard
                id="rate"
                title="Rate (r)"
                description={
                    <>
                        Average cycles between outputs. A solution which does not run indefinitely has no rate.
                        <p>This is often referred to as throughput, but is actually equal to 1/throughput. RG is also sometimes referred to as COFT ("cost optimized full throughput")</p>
                    </>
                }
            />
            <MetricCard
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
                    </>
                }
                image={{
                    location: overlapExplanation,
                    alt: "Overlap Explanation",
                }}
            />
            <MetricCard id="trackless" title="Trackless (T)" description={<>Solutions containing no tracks are classified as trackless.</>} />
        </Grid>
    )
}

interface MetricCardProps {
    id: string
    title: string
    description: JSX.Element
    image?: {
        location: string
        alt: string
    }
}

function MetricCard(props: MetricCardProps) {
    const [linkVisibility, setLinkVisibility] = useState<"hidden" | "visible">("hidden")
    const theme = useTheme()
    console.log(theme.mixins.toolbar)
    return (
        <Grid item xs key={props.id}>
            <div
                id={props.id}
                style={{
                    display: "block",
                    position: "relative",
                    top: `-${(theme.mixins.toolbar.minHeight as number) + 24}px`,
                    visibility: "hidden",
                }}
            />
            <Card
                sx={{
                    minWidth: "min(90vw, 460px)",
                    maxWidth: "100%",
                    height: "100%",
                }}
                onMouseEnter={() => setLinkVisibility("visible")}
                onMouseLeave={() => setLinkVisibility("hidden")}
            >
                <CardHeader
                    title={
                        <>
                            {props.title}
                            <HashLink to={`#${props.id}`} style={{ visibility: linkVisibility, color: theme.palette.text.primary, padding: "0.5rem" }}>
                                <LinkOutlined fontSize={"small"} />
                            </HashLink>
                        </>
                    }
                />
                {props.image && <CardMedia autoPlay loop muted component={props.image.location.endsWith(".mp4") ? "video" : "img"} src={props.image.location} alt={props.image.alt} />}
                <CardContent>{props.description}</CardContent>
            </Card>
        </Grid>
    )
}
