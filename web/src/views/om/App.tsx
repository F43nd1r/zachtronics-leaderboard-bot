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

import { Divider, IconButton, styled, SwipeableDrawer, Toolbar, Typography, useTheme } from "@mui/material"
import MenuIcon from "@mui/icons-material/Menu"
import ChevronLeftIcon from "@mui/icons-material/ChevronLeft"
import ChevronRightIcon from "@mui/icons-material/ChevronRight"
import useMediaQuery from "@mui/material/useMediaQuery"
import MuiAppBar, { AppBarProps as MuiAppBarProps } from "@mui/material/AppBar"
import Sidebar from "../../fragments/Sidebar"
import { Link, Outlet } from "react-router-dom"
import { usePersistedState } from "../../utils/usePersistedState"
import SearchBar from "../../fragments/SearchBar"
import { Settings } from "@mui/icons-material"

const drawerWidth = 300

interface AppBarProps extends MuiAppBarProps {
    open?: boolean
}

const AppBar = styled(MuiAppBar, {
    shouldForwardProp: (prop) => prop !== "open",
})<AppBarProps>(({ theme, open }) => ({
    transition: theme.transitions.create(["margin", "width"], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    ...(open && {
        [theme.breakpoints.up("md")]: {
            width: `calc(100% - ${drawerWidth}px)`,
            marginLeft: `${drawerWidth}px`,
            transition: theme.transitions.create(["margin", "width"], {
                easing: theme.transitions.easing.easeOut,
                duration: theme.transitions.duration.enteringScreen,
            }),
        },
    }),
}))

const DrawerHeader = styled("div")(({ theme }) => ({
    display: "flex",
    alignItems: "center",
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
    justifyContent: "space-between",
}))

const Main = styled("main", { shouldForwardProp: (prop) => prop !== "open" })<{
    open?: boolean
}>(({ theme, open }) => ({
    flexGrow: 1,
    padding: theme.spacing(2),
    transition: theme.transitions.create("margin", {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
    }),
    marginLeft: 0,
    ...(open && {
        [theme.breakpoints.up("md")]: {
            transition: theme.transitions.create("margin", {
                easing: theme.transitions.easing.easeOut,
                duration: theme.transitions.duration.enteringScreen,
            }),
            marginLeft: `${drawerWidth}px`,
        },
    }),
    maxWidth: "100%",
    display: "flex",
    flexDirection: "column",
}))

export default function App() {
    const theme = useTheme()
    const isNotTinyScreen = useMediaQuery(theme.breakpoints.up("sm"))
    const isAtLeastMediumScreen = useMediaQuery(theme.breakpoints.up("md"))
    const [open, setOpen] = usePersistedState("sidebarOpen", isNotTinyScreen)
    const handleDrawerOpen = () => setOpen(true)
    const handleDrawerClose = () => setOpen(false)
    return (
        <>
            <AppBar position="fixed" open={open}>
                <Toolbar disableGutters={true} sx={{ paddingLeft: 2, paddingRight: 2 }}>
                    {!open && (
                        <IconButton size="large" edge="start" color="inherit" aria-label="menu" onClick={handleDrawerOpen} sx={{ mr: 1 }}>
                            <MenuIcon />
                        </IconButton>
                    )}
                    {isNotTinyScreen && (
                        <Typography sx={{ fontWeight: "bold" }} paddingRight={"1rem"}>
                            Opus Magnum Leaderboards
                        </Typography>
                    )}
                    <SearchBar />
                </Toolbar>
            </AppBar>
            <SwipeableDrawer
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    "& .MuiDrawer-paper": {
                        width: drawerWidth,
                        boxSizing: "border-box",
                    },
                    display: "flex",
                    flexDirection: "column",
                }}
                variant={isAtLeastMediumScreen ? "persistent" : "temporary"}
                anchor="left"
                open={open}
                onOpen={handleDrawerOpen}
                onClose={handleDrawerClose}
            >
                <DrawerHeader>
                    <UnstyledLink to="settings">
                        <IconButton sx={{ ml: 1 }} color="inherit">
                            <Settings />
                        </IconButton>
                    </UnstyledLink>
                    <IconButton onClick={handleDrawerClose}>{theme.direction === "ltr" ? <ChevronLeftIcon /> : <ChevronRightIcon />}</IconButton>
                </DrawerHeader>
                <Divider />
                <Sidebar />
            </SwipeableDrawer>
            <Main open={open}>
                <DrawerHeader />
                <Outlet />
            </Main>
        </>
    )
}

const UnstyledLink = styled(Link, {})`
    color: inherit;
`
