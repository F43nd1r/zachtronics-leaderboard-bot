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

import { Divider, List, ListItemText } from "@mui/material"
import ExpandableListItem from "../components/ExpandableListItem"
import { Extension, Folder } from "@mui/icons-material"
import CategoryIcon from "@mui/icons-material/Category"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import LinkListItem from "../components/LinkListItem"
import Group from "../model/Group"
import Puzzle from "../model/Puzzle"
import Category from "../model/Category"
import { useParams } from "react-router-dom"
import ApiListResource from "../utils/ApiListResource"

function Groups() {
    return (
        <List>
            <ExpandableListItem
                title={"Puzzles"}
                icon={<Extension />}
                items={ApiListResource<Group[]>({
                    url: "/groups",
                    element: (groups) => groups.map((group) => <Puzzles group={group} key={group.id} sx={{ pl: 4 }} />),
                })}
            />
        </List>
    )
}

interface PuzzlesProps {
    group: Group
    sx?: SxProps<Theme>
}

function Puzzles(props: PuzzlesProps) {
    const puzzleId = useParams().puzzleId

    return (
        <List sx={props.sx}>
            <ExpandableListItem
                title={props.group.displayName}
                icon={<Folder />}
                items={ApiListResource<Puzzle[]>({
                    url: `/group/${props.group.id}/puzzles`,
                    element: (puzzles) =>
                        puzzles.map((puzzle) => (
                            <LinkListItem sx={{ pl: 4 }} key={puzzle.id} to={`/puzzles/${puzzle.id}`} selected={puzzleId === puzzle.id}>
                                <ListItemText primary={puzzle.displayName} />
                            </LinkListItem>
                        )),
                })}
            />
        </List>
    )
}

function Categories() {
    const categoryId = useParams().categoryId

    return (
        <List>
            <ExpandableListItem
                title={"Categories"}
                icon={<CategoryIcon />}
                items={ApiListResource<Category[]>({
                    url: "/categories",
                    element: (categories) =>
                        categories.map((category) => (
                            <LinkListItem sx={{ pl: 4 }} key={category.id} to={`/categories/${category.id}`} selected={categoryId === category.id}>
                                <ListItemText primary={`${category.displayName} (${category.metrics.join("→")})`} />
                            </LinkListItem>
                        )),
                })}
            />
        </List>
    )
}

export default function Sidebar() {
    return (
        <>
            <Groups />
            <Divider />
            <Categories />
        </>
    )
}
