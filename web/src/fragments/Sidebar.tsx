/*
 * Copyright (c) 2021
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import { Box, Divider, List, ListItemText, useTheme } from "@mui/material"
import ExpandableListItem from "../components/ExpandableListItem"
import { Extension, Folder } from "@mui/icons-material"
import CategoryIcon from "@mui/icons-material/Category"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import LinkListItem from "../components/LinkListItem"
import Group from "../model/Group"
import Puzzle from "../model/Puzzle"
import Category from "../model/Category"
import { Link, useMatch } from "react-router-dom"
import ApiResource from "../utils/ApiResource"

function Groups() {
    const match = useMatch("/puzzles/*")

    return (
        <List>
            <ExpandableListItem
                title={"Puzzles"}
                icon={<Extension />}
                content={ApiResource<Puzzle[]>({
                    url: "/puzzles",
                    element: (puzzles) => (
                        <>
                            {[
                                ...puzzles
                                    .reduce<Map<string, Puzzle[]>>((acc, puzzle) => {
                                        if (!acc.has(puzzle.group.id)) acc.set(puzzle.group.id, [puzzle])
                                        else acc.get(puzzle.group.id)!.push(puzzle)
                                        return acc
                                    }, new Map())
                                    .entries(),
                            ].map(([group, puzzles]) => (
                                <Puzzles group={puzzles[0].group} puzzles={puzzles} key={group} sx={{ pl: 4 }} />
                            ))}
                        </>
                    ),
                })}
                open={match !== null}
            />
        </List>
    )
}

interface PuzzlesProps {
    group: Group
    puzzles: Puzzle[]
    sx?: SxProps<Theme>
}

function Puzzles(props: PuzzlesProps) {
    const match = useMatch("/puzzles/:puzzleId/*")
    const puzzleId = match?.params?.puzzleId
    return (
        <List sx={props.sx}>
            <ExpandableListItem
                title={props.group.displayName}
                icon={<Folder />}
                content={
                    <>
                        {props.puzzles.map((puzzle) => (
                            <LinkListItem sx={{ pl: 4 }} key={puzzle.id} to={`/puzzles/${puzzle.id}`} selected={puzzleId === puzzle.id}>
                                <ListItemText primary={puzzle.displayName} />
                            </LinkListItem>
                        ))}
                    </>
                }
                open={props.puzzles.some((puzzle) => puzzleId === puzzle.id)}
            />
        </List>
    )
}

function Categories() {
    const match = useMatch("/categories/:categoryId/*")
    const categoryId = match?.params?.categoryId
    return (
        <List>
            <ExpandableListItem
                title={"Categories"}
                icon={<CategoryIcon />}
                content={ApiResource<Category[]>({
                    url: "/categories",
                    element: (categories) => (
                        <>
                            {categories
                                .sort((a, b) => (a.puzzleTypes.includes("PRODUCTION") && !b.puzzleTypes.includes("PRODUCTION") ? 1 : -1))
                                .map((category) => (
                                    <LinkListItem sx={{ pl: 4 }} key={category.id} to={`/categories/${category.id}`} selected={categoryId === category.id}>
                                        <ListItemText primary={`${category.displayName} (${category.metrics.join("→")})${category.puzzleTypes.includes("PRODUCTION") ? " (Production)" : ""}`} />
                                    </LinkListItem>
                                ))}
                        </>
                    ),
                })}
                open={categoryId !== undefined}
            />
        </List>
    )
}

export default function Sidebar() {
    const theme = useTheme()
    // noinspection HtmlUnknownTarget
    return (
        <>
            <Groups />
            <Divider />
            <Categories />
            <Box sx={{ flexGrow: 1 }} />
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                }}
            >
                <Link to="/help" style={{ padding: "1rem", color: theme.palette.primary.main }}>
                    Help
                </Link>
                <Link to="/swagger-ui.html" style={{ padding: "1rem", color: theme.palette.primary.main }}>
                    API docs
                </Link>
            </Box>
        </>
    )
}
