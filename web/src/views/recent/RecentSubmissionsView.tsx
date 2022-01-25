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
import { useMemo } from "react"
import ApiResource from "../../utils/ApiResource"
import RecordGrid from "../../components/RecordGrid"
import { usePersistedNumberState } from "../../utils/usePersistedState"
import { Box, FormControl, InputLabel, MenuItem, Select } from "@mui/material"
import OmRecordChange from "../../model/om/OmRecordChange"

enum SinceLast {
    DAY = 1000 * 60 * 60 * 24,
    WEEK = DAY * 7,
    MONTH = DAY * 30,
}

export default function RecentSubmissionsView() {
    const [sinceLast, setSinceLast] = usePersistedNumberState<SinceLast>("submissionsSince", SinceLast.WEEK)

    const dateSince: Date = useMemo(() => new Date(new Date().getTime() - sinceLast), [sinceLast])

    return (
        <Box
            sx={{
                display: "flex",
                height: "100%",
                width: "100%",
                flexDirection: "column",
            }}
        >
            <FormControl sx={{ width: "15rem", paddingBottom: 2 }}>
                <InputLabel id={`since-last-select-label`}>Show Submissions within last</InputLabel>
                <Select
                    value={sinceLast}
                    onChange={(event) => setSinceLast(event.target.value as SinceLast)}
                    labelId={`since-last-select-label`}
                    label={"Show Submissions within last"}
                    fullWidth={false}
                >
                    <MenuItem value={SinceLast.DAY} key={SinceLast.DAY}>
                        Day
                    </MenuItem>
                    <MenuItem value={SinceLast.WEEK} key={SinceLast.WEEK}>
                        Week
                    </MenuItem>
                    <MenuItem value={SinceLast.MONTH} key={SinceLast.MONTH}>
                        Month
                    </MenuItem>
                </Select>
            </FormControl>
            <ApiResource<OmRecordChange[]>
                url={`/records/changes/${dateSince.toISOString()}`}
                element={(changes) => (
                    <RecordGrid
                        records={changes.filter((change) => change.type === "ADD").map((change) => change.record)}
                        getTitle={(record) => record.puzzle.displayName}
                        getScore={(record) => record.fullFormattedScore ?? "None"}
                    />
                )}
            />
        </Box>
    )
}
