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
import { Autocomplete, TextField } from "@mui/material"
import { useEffect, useState } from "react"
import fetchFromApi from "../utils/fetchFromApi"
import Puzzle from "../model/Puzzle"
import Category from "../model/Category"
import { useNavigate } from "react-router-dom"

export default function SearchBar() {
    const navigate = useNavigate()
    const [options, setOptions] = useState<(Puzzle | Category)[]>([])
    useEffect(() => {
        Promise.all([fetchFromApi<Puzzle[]>("/om/puzzles"), fetchFromApi<Category[]>("/om/categories")]).then((data) => setOptions(data.flat()))
    }, [])
    return (
        <Autocomplete<Puzzle | Category>
            renderInput={(params) => <TextField {...params} label="Search…" variant="standard" sx={{}} />}
            options={options}
            groupBy={(option) => (isPuzzle(option) ? option.group.displayName : "Categories")}
            getOptionLabel={(option) => option.displayName}
            isOptionEqualToValue={(option, value) => option.id === value.id}
            renderOption={(props, option) => (
                <li {...props} key={option.id}>
                    {option.displayName}
                </li>
            )}
            sx={{
                flexGrow: 1,
                alignSelf: "start",
            }}
            onChange={(event, value) => {
                if (value) {
                    if (isPuzzle(value)) {
                        navigate(`/puzzles/${value.id}`)
                    } else {
                        navigate(`/categories/${value.id}`)
                    }
                }
            }}
        />
    )
}

function isPuzzle(test: any): test is Puzzle {
    return test.hasOwnProperty("group") && typeof test.group !== undefined
}
