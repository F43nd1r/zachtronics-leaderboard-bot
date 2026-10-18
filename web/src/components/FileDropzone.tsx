/*
 * Copyright (c) 2026
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

import {Box, Chip, Typography} from "@mui/material"
import {CloudUpload} from "@mui/icons-material"
import {useDropzone} from "react-dropzone"

interface FileDropzoneProps {
    file?: File
    onChange: (file: File | undefined) => void
    accept: string
    maxSize: number
    text: string
}

export default function FileDropzone(props: FileDropzoneProps) {
    const {getRootProps, getInputProps, isDragActive, fileRejections} = useDropzone({
        accept: {"application/octet-stream": [props.accept]},
        maxSize: props.maxSize,
        multiple: false,
        onDropAccepted: (files) => props.onChange(files[0]),
    })
    return (
        <Box
            {...getRootProps()}
            sx={{
                flexGrow: 1,
                minHeight: "8rem",
                p: 2,
                display: "flex",
                flexDirection: "column",
                alignItems: "center",
                justifyContent: "center",
                gap: 1,
                textAlign: "center",
                cursor: "pointer",
                border: "2px dashed",
                borderColor: isDragActive ? "primary.main" : "divider",
                borderRadius: 1,
            }}
        >
            <input {...getInputProps()} />
            <CloudUpload color="action" fontSize="large" />
            <Typography color="text.secondary">{props.text}</Typography>
            {props.file && <Chip label={props.file.name} onDelete={() => props.onChange(undefined)} onClick={(e) => e.stopPropagation()} />}
            {fileRejections.length > 0 && (
                <Typography color="error" variant="caption">
                    {fileRejections[0].errors[0].message}
                </Typography>
            )}
        </Box>
    )
}
