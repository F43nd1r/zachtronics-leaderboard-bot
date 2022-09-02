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

import { useAppSettings } from "../../../fragments/AppSettingsProvider"
import { IconButton } from "@mui/material"
import Brightness7Icon from "@mui/icons-material/Brightness7"
import Brightness4Icon from "@mui/icons-material/Brightness4"
import { PlayArrow, PlayDisabled, Visibility, VisibilityOff } from "@mui/icons-material"

export default function SettingsView() {
    const appSettings = useAppSettings()

    return (
        <div style={{ display: "flex", justifyContent: "center", alignItems: "center", width: "100%", height: "100%" }}>
            <div style={{ display: "flex", flexDirection: "column" }}>
                <div>
                    <IconButton sx={{ ml: 1 }} onClick={() => appSettings.setColorMode(appSettings.colorMode === "light" ? "dark" : "light")} color="inherit">
                        {appSettings.colorMode === "dark" ? <Brightness4Icon /> : <Brightness7Icon />}
                    </IconButton>{" "}
                    Color Theme
                </div>
                <div>
                    <IconButton sx={{ ml: 1 }} onClick={() => appSettings.setAutoPlay(!appSettings.autoPlay)} color="inherit">
                        {appSettings.autoPlay ? <PlayArrow /> : <PlayDisabled />}
                    </IconButton>{" "}
                    Autoplay
                </div>
                <div>
                    <IconButton sx={{ ml: 1 }} onClick={() => appSettings.setShowControls(!appSettings.showControls)} color="inherit">
                        {appSettings.showControls ? <Visibility /> : <VisibilityOff />}
                    </IconButton>{" "}
                    Show Controls
                </div>
            </div>
        </div>
    )
}
