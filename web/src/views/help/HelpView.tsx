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

import { Card, CardContent, CardHeader, CardMedia, useTheme } from "@mui/material"
import { LinkOutlined } from "@mui/icons-material"
import { useMemo, useState } from "react"
import { HashLink } from "react-router-hash-link"
import { useLocation } from "react-router-dom"
import { Masonry } from "@mui/lab"
import PageMeta from "../../components/PageMeta"
import heightExplanation from "./height_explanation.jpg"
import widthExplanation from "./width_explanation.jpg"
import overlapExplanation from "./overlap_tutorial.mp4"

export const helpCards: HelpCardProps[] = [
    { id: "cost", title: "Cost (g)", description: "Cost of all parts in the solution, as defined by the game." },
    { id: "cycles", title: "Cycles (c)", description: "Cycles needed to run the solution to completion, as defined by the game." },
    {
        id: "area",
        title: "Area (a)",
        description: `Area used by the solution when running to completion, as defined by the game.

Production puzzles don't show this value in game, but it is computed in the same way.`,
    },
    {
        id: "instructions",
        title: "Instructions (i)",
        description: `Instructions on all arms, as defined by the game.

Repeat and return instructions are expanded to their actual contained instructions. The wait instruction is not counted.

Non-production puzzles don't show this value in game, but it is computed in the same way.`,
    },
    { id: "sum", title: "Sum (g+c+a / g+c+i)", description: "Sum of cost, cycles and area (or instructions in production puzzles)." },
    { id: "sum4", title: "Sum4 (g+c+a+i)", description: "Sum of cost, cycles, area and instructions." },
    {
        id: "height",
        title: "Height (h)",
        description: `Number of rows in the area footprint of the solution.

Height is measured on all three axes, and the smallest value is taken. This means rotation of the solution doesn't matter.`,
        image: { location: heightExplanation, alt: "Height Explanation" },
    },
    {
        id: "width",
        title: "Width (w)",
        description: `Number of columns in the area footprint of the solution. Note that half hexes are possible in this metric (see image).

Width is measured on all three axes, and the smallest value is taken, same as height. This is often used to improve the aspect ratio of width gifs.`,
        image: { location: widthExplanation, alt: "Width Explanation" },
    },
    {
        id: "rate",
        title: "Rate (r)",
        description: `Average cycles between outputs. A solution which does not run indefinitely has no rate.

This is often referred to as throughput, but is actually equal to 1/throughput. RG is also sometimes referred to as COFT ("cost optimized full throughput")`,
    },
    {
        id: "overlap",
        title: "Overlap (O)",
        description: `Solutions containing overlapped parts are classified as Overlap.

The rules of the game would imply that components (inputs, outputs, glyphs, arms) cannot overlap with one another. However, this restriction is not enforced by the solution file, and solutions which violate it can still be run and recorded in game.

Solutions with this behavior can be created using an external editor, or you can rotate an object, drag it off screen, shift click another object, and drag it in place, to achieve overlap within the game itself as shown in the video.

When running solutions with this exploit, it is highly encouraged to enable Hermit Mode in the options, so as not to overwrite your existing scores.`,
        image: { location: overlapExplanation, alt: "Overlap Explanation" },
    },
    { id: "trackless", title: "Trackless (T)", description: "Solutions containing no tracks are classified as trackless." },
    {
        id: "pareto",
        title: "Pareto Frontier",
        description: `A solution is part of the pareto frontier of a puzzle if no other solution exists that is strictly better.

A solution is strictly better than another if it is at least equal in all metrics and better in at least one metric.

Displaying the full pareto frontier is not possible, as each metric would require a separate axis, but the visualizer allows you to view and filter 2D or 3D projections of it.

If you enable 'Show only frontier' you can view the pareto frontier as it would look like if only the selected metrics existed.

Mapping out the full pareto frontier for a puzzle is usually not possible, as an infinite amount of tradeoffs between metrics exist.`,
    },
    {
        id: "bot",
        title: "Discord Bot",
        description: `The Discord Bot allows you to submit or query records.

Use '/om submit solution:<solution link> gif:<gif link>' to submit your solution to the leaderboards.

If you want to use discord attachments instead of links, you can use 'mX' or 'mX.Y' in place of one or both of the links, where X refers to your Xth previous message (e.g. 'm1' refers to your last message) and Y refers to the Yth attachment on the message (e.g. 'm1.2' refers to the second attachment on your last message).

To view records, use '/om show puzzle:<puzzle name> category:<short category name>'.

To browse records, use either '/om list puzzle:<puzzle name>' if you're looking for only records, or '/om frontier puzzle:<puzzle name>'if you're looking for the full pareto frontier.`,
    },
]
export default function HelpView() {
    const hash = useLocation().hash
    const activeCard = useMemo(() => helpCards.find((card) => `#${card.id}` === hash), [hash])
    return (
        <Masonry spacing={3} columns={{ xs: 1, sm: 2, md: 2, lg: 2, xl: 3, xxl: 3, xxxl: 4 }}>
            <PageMeta
                title={`Help${activeCard && `: ${activeCard.title}`}`}
                description={activeCard ? activeCard.description : "Read about Opus Magnum Metrics and more"}
                image={activeCard?.image?.location}
            />
            {helpCards.map((card) => (
                <HelpCard key={card.id} {...card} />
            ))}
        </Masonry>
    )
}

interface HelpCardProps {
    id: string
    title: string
    description: string
    image?: {
        location: string
        alt: string
    }
}

function HelpCard(props: HelpCardProps) {
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
            <CardContent sx={{ whiteSpace: "pre-line" }}>{props.description}</CardContent>
        </Card>
    )
}
