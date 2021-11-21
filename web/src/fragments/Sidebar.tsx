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

import { Divider, List, ListItem, ListItemIcon, ListItemText } from "@mui/material"
import ExpandableListItem from "../components/ExpandableListItem"
import React, { useEffect, useState } from "react"
import { ComponentState } from "../utils/ComponentState"
import { Error, Extension, Folder } from "@mui/icons-material"
import CategoryIcon from "@mui/icons-material/Category"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import LinkListItem from "../components/LinkListItem"
import Group from "../model/Group"
import Puzzle from "../model/Puzzle"
import Category from "../model/Category"
import LoadingIndicator from "../components/LoadingIndicator"
import fetchFromApi from "../utils/fetchFromApi"
import { useParams } from "react-router-dom"

function Groups() {
    const [state, setState] = useState(ComponentState.LOADING)
    const [groups, setGroups] = useState<Group[]>([])
    useEffect(() => {
        fetchFromApi<Group[]>("/groups", setState).then((data) => setGroups(data))
    }, [])

    let content: JSX.Element[]
    switch (state) {
        case ComponentState.LOADING:
            content = [<LoadingIndicator />]
            break
        case ComponentState.ERROR:
            content = [
                <ListItem sx={{ pl: 4 }} key={"error"}>
                    <ListItemIcon>
                        <Error />
                    </ListItemIcon>
                    <ListItemText primary="Failed to load groups" />
                </ListItem>,
            ]
            break
        case ComponentState.READY:
            content = groups.map((group) => <Puzzles group={group} key={group.id} sx={{ pl: 4 }} />)
            break
    }

    return (
        <List>
            <ExpandableListItem title={"Puzzles"} icon={<Extension />} items={content} />
        </List>
    )
}

interface PuzzlesProps {
    group: Group
    sx?: SxProps<Theme>
}

function Puzzles(props: PuzzlesProps) {
    const [state, setState] = useState(ComponentState.LOADING)
    const [puzzles, setPuzzles] = useState<Puzzle[]>([])
    useEffect(() => {
        fetchFromApi<Puzzle[]>(`/group/${props.group.id}/puzzles`, setState).then((data) => setPuzzles(data))
    }, [props.group.id])
    const params = useParams()
    const puzzleId = params.puzzleId

    let content: JSX.Element[]
    switch (state) {
        case ComponentState.LOADING:
            content = [<LoadingIndicator />]
            break
        case ComponentState.ERROR:
            content = [
                <ListItem sx={{ pl: 4 }} key={"error"}>
                    <ListItemIcon>
                        <Error />
                    </ListItemIcon>
                    <ListItemText primary="Failed to load puzzles" />
                </ListItem>,
            ]
            break
        case ComponentState.READY:
            content = puzzles.map((puzzle) => (
                <LinkListItem sx={{ pl: 4 }} key={puzzle.id} to={`/puzzles/${puzzle.id}`} selected={puzzleId === puzzle.id}>
                    <ListItemText primary={puzzle.displayName} />
                </LinkListItem>
            ))
            break
    }

    return (
        <List sx={props.sx}>
            <ExpandableListItem title={props.group.displayName} icon={<Folder />} items={content} />
        </List>
    )
}

function Categories() {
    const [state, setState] = useState(ComponentState.LOADING)
    const [categories, setCategories] = useState<Category[]>([])
    useEffect(() => {
        fetchFromApi<Category[]>("/categories", setState).then((data) => setCategories(data))
    }, [])
    const params = useParams()
    const categoryId = params.categoryId

    let content: JSX.Element[]
    switch (state) {
        case ComponentState.LOADING:
            content = [<LoadingIndicator />]
            break
        case ComponentState.ERROR:
            content = [
                <ListItem sx={{ pl: 4 }} key={"error"}>
                    <ListItemIcon>
                        <Error />
                    </ListItemIcon>
                    <ListItemText primary="Failed to load categories" />
                </ListItem>,
            ]
            break
        case ComponentState.READY:
            content = categories.map((category) => (
                <LinkListItem sx={{ pl: 4 }} key={category.id} to={`/categories/${category.id}`} selected={categoryId === category.id}>
                    <ListItemText primary={`${category.displayName} (${category.metrics.join("→")})`} />
                </LinkListItem>
            ))
            break
    }

    return (
        <List>
            <ExpandableListItem title={"Categories"} icon={<CategoryIcon />} items={content} />
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
