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

import { Alert, Backdrop, Box, CircularProgress, Grow, TextField } from "@mui/material"
import { DropzoneAreaBase, FileObject } from "react-mui-dropzone"
import { useEffect, useState } from "react"
import { fetchFromApiRaw } from "../../../utils/fetchFromApi"
import { CheckCircle } from "@mui/icons-material"
import { usePersistedState } from "../../../utils/usePersistedState"

export default function UploadView() {
    const [files, setFiles] = useState<FileObject[]>([])
    const [author, setAuthor] = usePersistedState<string>("om-upload-author", "")
    const [error, setError] = useState<string>()
    const [isUploading, setIsUploading] = useState<boolean>(false)
    const [success, setSuccess] = useState<boolean>(false)
    useEffect(() => {
        const processFiles = async () => {
            setError(undefined)
            if (files.length === 2) {
                const solution = files.find((fileObject) => fileObject.file.name.endsWith(".solution"))
                if (!author) {
                    setError("You need to enter your name.")
                    return
                }
                if (!solution) {
                    setError("One of your files must be a solution file.")
                    return
                }
                const gif = files.find((fileObject) => fileObject.file.name.endsWith(".gif"))
                if (!gif) {
                    setError("One of your files must be a gif.")
                    return
                }
                setIsUploading(true)
                const formData = new FormData()
                formData.append("author", author)
                formData.append("solution", new Blob([await solution.file.arrayBuffer()]))
                formData.append("gifData", new Blob([await gif.file.arrayBuffer()]))
                const response = await fetchFromApiRaw("/om/submit", {
                    method: "POST",
                    body: formData,
                })
                const result = await response.json()
                if (!response.ok || result !== "SUCCESS") {
                    setError(`Upload failed: ${JSON.stringify(result)}`)
                } else {
                    setSuccess(true)
                    await new Promise((resolve) => setTimeout(resolve, 2000))
                    setFiles([])
                    setSuccess(false)
                }
                setIsUploading(false)
            }
        }
        processFiles().then()
    }, [author, files])
    return (
        <div
            style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                height: "100%",
            }}
        >
            <Box
                sx={{
                    padding: 4,
                    maxWidth: "800px",
                    display: "flex",
                    flexDirection: "column",
                    minHeight: 0,
                    flexGrow: 1,
                    flexShrink: 1,
                    gap: "1em",
                }}
            >
                <TextField
                    id="author"
                    label="Author"
                    variant="outlined"
                    fullWidth
                    autoFocus
                    onChange={(e) => setAuthor(e.target.value)}
                    defaultValue={author}
                />
                <DropzoneAreaBase
                    fileObjects={files}
                    onAdd={(newFiles) => {
                        setFiles([...files, ...newFiles])
                    }}
                    onDelete={(removeFile) => {
                        setFiles(files.filter((file) => file !== removeFile))
                    }}
                    filesLimit={2}
                    maxFileSize={104857600}
                    dropzoneText="Drop your solution and gif here"
                    showPreviews={true}
                    showPreviewsInDropzone={false}
                    previewText="Files:"
                    showFileNames={true}
                    showFileNamesInPreview={true}
                    useChipsForPreview={true}
                />
                {error ? <Alert severity="error">{error}</Alert> : undefined}
                <Backdrop open={isUploading} color={"#000000"}>
                    {success ? (
                        <Grow in {...(success ? { timeout: 1000 } : {})}>
                            <CheckCircle color={"success"} sx={{ fontSize: 200 }} />
                        </Grow>
                    ) : (
                        <CircularProgress color={"inherit"} size={"50px"} />
                    )}
                </Backdrop>
            </Box>
        </div>
    )
}
