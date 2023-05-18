/*
 * Copyright (c) 2023
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

/**
 * a map that uses a custom function for key equality
 */
export class CustomMap<K, V> implements Map<K, V> {
    private readonly idFun: (key: K) => string
    private _values: Map<string, V> = new Map<string, V>()
    private _keys: Map<string, K> = new Map<string, K>()

    constructor(idFun: (key: K) => string = JSON.stringify) {
        this.idFun = idFun
    }

    public set(key: K, value: V): this {
        const id = this.idFun(key)
        this._values.set(id, value)
        this._keys.set(id, key)
        return this
    }

    public get(key: K): V | undefined {
        return this._values.get(this.idFun(key))
    }

    public has(key: K): boolean {
        return this._values.has(this.idFun(key))
    }

    public delete(key: K): boolean {
        const id = this.idFun(key)
        this._keys.delete(id)
        return this._values.delete(id)
    }

    public clear(): void {
        this._values.clear()
        this._keys.clear()
    }

    public get size(): number {
        return this._values.size
    }

    public keys(): IterableIterator<K> {
        return this._keys.values()
    }

    public values(): IterableIterator<V> {
        return this._values.values()
    }

    public entries(): IterableIterator<[K, V]> {
        return mapIterator(this._values.entries(), (value) => [this._keys.get(value[0])!, value[1]])
    }

    public [Symbol.iterator](): IterableIterator<[K, V]> {
        return this.entries()
    }

    public forEach(callbackfn: (value: V, key: K, map: CustomMap<K, V>) => void, thisArg?: any): void {
        for (let entry of this.entries()) {
            callbackfn.call(thisArg, entry[1], entry[0], this)
        }
    }

    public [Symbol.toStringTag]: string = "CustomMap"

    public mapKeys<T>(callback: (key: K) => T): T[] {
        return [...mapIterator(this.keys(), callback)]
    }

    public mapValues<T>(callback: (value: V) => T): T[] {
        return [...mapIterator(this.values(), callback)]
    }

    public map<T>(callback: (key: K, value: V) => T): T[] {
        return [...mapIterator(this.entries(), (entry) => callback(entry[0], entry[1]))]
    }

    // ----------------- functional utilities -----------------

    public someKey(predicate: (key: K) => boolean): boolean {
        for (let key of this.keys()) {
            if (predicate(key)) {
                return true
            }
        }
        return false
    }

    public someValue(predicate: (value: V) => boolean): boolean {
        for (let value of this.values()) {
            if (predicate(value)) {
                return true
            }
        }
        return false
    }

    public some(predicate: (key: K, value: V) => boolean): boolean {
        for (let entry of this.entries()) {
            if (predicate(entry[0], entry[1])) {
                return true
            }
        }
        return false
    }
}

function* mapIterator<T1, T2>(iterable: IterableIterator<T1>, callback: (value: T1) => T2): IterableIterator<T2> {
    for (let x of iterable) {
        yield callback(x)
    }
}
