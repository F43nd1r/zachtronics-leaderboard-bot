/*
 * Copyright (c) 2021
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

import { Collapse, List, ListItemButton, ListItemIcon, ListItemText } from "@mui/material"
import { ExpandLess, ExpandMore } from "@mui/icons-material"
import { SxProps } from "@mui/system"
import { Theme } from "@mui/material/styles"
import { useEffect, useState } from "react"

interface ExpandableListItemProps {
    title: string
    icon?: JSX.Element
    items: JSX.Element[]
    sx?: SxProps<Theme>
    open?: boolean
}

export default function ExpandableListItem(props: ExpandableListItemProps) {
    const [open, setOpen] = useState<boolean>(false)
    useEffect(() => {
        if (props.open !== undefined) setOpen(props.open)
    }, [setOpen, props.open])
    const handleClick = () => {
        setOpen(!open)
    }
    console.log(`${props.title} shouldOpen: ${props.open}, isOpen: ${open}`)
    return (
        <>
            <ListItemButton onClick={handleClick} sx={props.sx}>
                {props.icon && <ListItemIcon>{props.icon}</ListItemIcon>}
                <ListItemText primary={props.title} />
                {open ? <ExpandLess /> : <ExpandMore />}
            </ListItemButton>
            <Collapse in={open} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                    {props.items}
                </List>
            </Collapse>
        </>
    )
}
