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

import {
    Alert,
    Backdrop,
    Box,
    Button,
    Checkbox,
    CircularProgress,
    FormControlLabel,
    Grow,
    TextField
} from "@mui/material"
import {DropzoneAreaBase, FileObject} from "react-mui-dropzone"
import {useState} from "react"
import {fetchFromApiRaw} from "../../../utils/fetchFromApi"
import {CheckCircle} from "@mui/icons-material"
import {usePersistedState} from "../../../utils/usePersistedState"
import {Type} from "@sinclair/typebox"

export default function UploadView() {
    const [solutionFile, setSolutionFile] = useState<FileObject | undefined>(undefined)
    const [gifFile, setGifFile] = useState<FileObject | undefined>(undefined)
    const [author, setAuthor] = usePersistedState("om-upload-author", Type.String(), "")
    const [allowGifUpdate, setAllowGifUpdate] = useState<boolean>(false)
    const [error, setError] = useState<string>()
    const [isUploading, setIsUploading] = useState<boolean>(false)
    const [success, setSuccess] = useState<boolean>(false)

    const handleUpload = async () => {
        setError(undefined)
        if (!solutionFile) {
            setError("Please drop your solution file.")
            return
        }
        if (!gifFile) {
            setError("Please drop your GIF file.")
            return
        }
        if (!author) {
            setError("You need to enter your name.")
            return
        }

        setIsUploading(true)
        const formData = new FormData()
        formData.append("author", author)
        formData.append("solution", new Blob([await solutionFile.file.arrayBuffer()]))
        formData.append("gifData", new Blob([await gifFile.file.arrayBuffer()]))
        formData.append("allowGifUpdate", allowGifUpdate.toString());
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
            setSolutionFile(undefined)
            setGifFile(undefined)
            setSuccess(false)
        }
        setIsUploading(false)
    }

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
                    maxWidth: "1000px",
                    display: "flex",
                    flexDirection: "column",
                    minHeight: 0,
                    flexGrow: 1,
                    flexShrink: 1,
                    gap: "1em",
                }}
            >
                <Box sx={{ display: "flex", alignItems: "center", gap: "1em" }}>
                    <TextField
                        id="author"
                        label="Author"
                        variant="outlined"
                        fullWidth
                        autoFocus
                        onChange={(e) => setAuthor(e.target.value)}
                        defaultValue={author}
                    />
                    <FormControlLabel
                        sx={{ whiteSpace: 'nowrap' }}
                        control={
                            <Checkbox
                                checked={allowGifUpdate}
                                onChange={(e) => setAllowGifUpdate(e.target.checked)}
                                name="allowGifUpdate"
                                color="primary"
                            />
                        }
                        label="Allow GIF Update"
                    />
                </Box>
                <Box sx={{ display: "flex", gap: "1em" }}>
                    <DropzoneAreaBase
                        fileObjects={solutionFile ? [solutionFile] : []}
                        onAdd={(newFiles) => {
                            if (newFiles.length > 0) setSolutionFile(newFiles[0])
                        }}
                        onDelete={() => {
                            setSolutionFile(undefined)
                        }}
                        filesLimit={1}
                        acceptedFiles={[".solution"]}
                        maxFileSize={1024*1024}
                        dropzoneText="Drop your solution file here"
                        showPreviewsInDropzone={true}
                        useChipsForPreview={true}
                        showFileNames={true}
                    />
                    <DropzoneAreaBase
                        fileObjects={gifFile ? [gifFile] : []}
                        onAdd={(newFiles) => {
                            if (newFiles.length > 0) setGifFile(newFiles[0])
                        }}
                        onDelete={() => {
                            setGifFile(undefined)
                        }}
                        filesLimit={1}
                        acceptedFiles={[".gif"]}
                        maxFileSize={100*1024*1024}
                        dropzoneText="Drop your GIF file here"
                        showPreviewsInDropzone={true}
                        useChipsForPreview={true}
                        showFileNames={true}
                    />
                </Box>
                <Button
                    variant="contained"
                    color="primary"
                    onClick={handleUpload}
                    disabled={isUploading || !solutionFile || !gifFile || !author}
                    type="submit"
                >
                    Upload
                </Button>
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
