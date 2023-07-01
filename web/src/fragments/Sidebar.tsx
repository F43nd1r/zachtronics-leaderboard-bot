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

import { Box, Divider, Link as ExternalLink, List, ListItemText, useTheme } from "@mui/material"
import ExpandableListItem from "../components/ExpandableListItem"
import { Extension, Folder } from "@mui/icons-material"
import CategoryIcon from "@mui/icons-material/Category"
import LinkListItem from "../components/LinkListItem"
import Group from "../model/Group"
import Puzzle from "../model/Puzzle"
import Category from "../model/Category"
import { Link, useMatch } from "react-router-dom"
import ApiResource from "../utils/ApiResource"
import Collection from "../model/Collection"
import { CustomMap } from "../utils/CustomMap"
import Manifold from "../model/Manifold"

function groupPuzzles(puzzles: Puzzle[]) {
    return puzzles.reduce<CustomMap<Collection, CustomMap<Group, Puzzle[]>>>((acc, puzzle) => {
        if (!acc.has(puzzle.group.collection)) acc.set(puzzle.group.collection, new CustomMap((group) => group.id))
        const collection = acc.get(puzzle.group.collection)!
        if (!collection.has(puzzle.group)) collection.set(puzzle.group, [puzzle])
        else collection.get(puzzle.group)!.push(puzzle)
        return acc
    }, new CustomMap((collection) => collection.id))
}

function Groups() {
    const match = useMatch("/puzzles/:puzzleId/*")
    const puzzleId = match?.params?.puzzleId

    return (
        <List>
            <ApiResource<Puzzle[]>
                url={"/om/puzzles"}
                element={(puzzles) =>
                    groupPuzzles(puzzles).map((collection, groups) => (
                        <ExpandableListItem
                            key={collection.id}
                            title={`${collection.displayName} Puzzles`}
                            icon={<Extension />}
                            content={groups.map((group, puzzles) => (
                                <Puzzles group={group} puzzles={puzzles} key={group.id} selectedPuzzleId={puzzleId} />
                            ))}
                            open={groups.someValue((puzzles) => puzzles.some((puzzle) => puzzleId === puzzle.id))}
                        />
                    ))
                }
            />
        </List>
    )
}

interface PuzzlesProps {
    group: Group
    puzzles: Puzzle[]
    selectedPuzzleId: string | undefined
}

function Puzzles({ group, puzzles, selectedPuzzleId }: PuzzlesProps) {
    return (
        <List sx={{ pl: 4 }}>
            <ExpandableListItem
                title={group.displayName}
                icon={<Folder />}
                content={puzzles.map((puzzle) => (
                    <LinkListItem sx={{ pl: 4 }} key={puzzle.id} to={`/puzzles/${puzzle.id}`} selected={selectedPuzzleId === puzzle.id}>
                        <ListItemText primary={puzzle.displayName} />
                    </LinkListItem>
                ))}
                open={puzzles.some((puzzle) => selectedPuzzleId === puzzle.id)}
            />
        </List>
    )
}

function groupCategories(categories: Category[]) {
    return categories.reduce<CustomMap<Manifold, Category[]>>((acc, category) => {
        if (!acc.has(category.manifold)) acc.set(category.manifold, [category])
        else acc.get(category.manifold)!.push(category)
        return acc
    }, new CustomMap((manifold) => manifold.id))
}

function Categories() {
    const match = useMatch("/categories/:categoryId/*")
    const categoryId = match?.params?.categoryId
    return (
        <List>
            <ApiResource<Category[]>
                url={"/om/categories"}
                element={(categories) =>
                    groupCategories(categories).map((manifold, categories) => (
                        <ExpandableListItem
                            key={manifold.id}
                            title={`Categories ${manifold.displayName}`}
                            icon={<CategoryIcon />}
                            content={categories
                                .sort((a, b) => (a.puzzleTypes.includes("PRODUCTION") && !b.puzzleTypes.includes("PRODUCTION") ? 1 : -1))
                                .map((category) => (
                                    <LinkListItem sx={{ pl: 4 }} key={category.id} to={`/categories/${category.id}`} selected={categoryId === category.id}>
                                        <ListItemText
                                            primary={`${category.displayName} (${category.metrics.join("→")})${
                                                category.puzzleTypes.includes("PRODUCTION") ? " (Production)" : ""
                                            }`}
                                        />
                                    </LinkListItem>
                                ))}
                            open={categories.some((category) => categoryId === category.id)}
                        />
                    ))
                }
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
                <ExternalLink href="/swagger-ui/index.html" style={{ padding: "1rem", color: theme.palette.primary.main }}>
                    API docs
                </ExternalLink>
            </Box>
        </>
    )
}
