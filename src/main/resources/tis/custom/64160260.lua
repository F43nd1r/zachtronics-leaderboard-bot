-- The function get_name() should return a single string that is the name of the puzzle.
--
function get_name()
	return "INTERSECTION IDENTIFIER"
end

-- The function get_description() should return an array of strings, where each string is
-- a line of description for the puzzle. The text you return from get_description() will 
-- be automatically formatted and wrapped to fit inside the puzzle information box.
--
function get_description()
	return { "IDENTIFY WHICH NUMBERS ARE PRESENT IN ALL THREE SEQUENCES", "OUTPUT THOSE NUMBERS IN THE ORDER THEY APPEAR IN EACH SEQUENCE", "SEQUENCES ARE ZERO TERMINATED" }
end

-- The function get_streams() should return an array of streams. Each stream is described
-- by an array with exactly four values: a STREAM_* value, a name, a position, and an array
-- of integer values between -999 and 999 inclusive.
--
-- STREAM_INPUT: An input stream containing up to 39 numerical values.
-- STREAM_OUTPUT: An output stream containing up to 39 numerical values.
-- STREAM_IMAGE: An image output stream, containing exactly 30*18 numerical values between 0
--               and 4, representing the full set of "pixels" for the target image.
--
-- NOTE: Arrays in Lua are implemented as tables (dictionaries) with integer keys that start
--       at 1 by convention. The sample code below creates an input array of 39 random values
--       and an output array that doubles all of the input values.
--
-- NOTE: To generate random values you should use math.random(). However, you SHOULD NOT seed
--       the random number generator with a new seed value, as that is how TIS-100 ensures that
--       the first test run is consistent for all users, and thus something that allows for the
--       comparison of cycle scores.
--
-- NOTE: Position values for streams should be between 0 and 3, which correspond to the far
--       left and far right of the TIS-100 segment grid. Input streams will be automatically
--       placed on the top, while output and image streams will be placed on the bottom.
--
function get_streams()

-- Start with the desired output order - controlled values will appear in the same order in all 3
	local outLength = math.random(3, 7)
	outOrder = {
		{},
		{},
		{},
	}
	for i = 1, outLength do
		outOrder[1][i] = i
		outOrder[2][i] = i
		outOrder[3][i] = i
	end

-- Add one falsie for each pair of sequences - it's a little messy I guess
	local falsie12 = math.random(1, outLength + 1)
	table.insert(outOrder[1], falsie12, outLength + 1)
	table.insert(outOrder[2], falsie12, outLength + 1)

	local falsie23 = math.random(1, outLength + 1)
	if falsie23 > falsie12 then
		table.insert(outOrder[2], falsie23 + 1, outLength + 2)
	else
		table.insert(outOrder[2], falsie23, outLength + 2)
	end
	table.insert(outOrder[3], falsie23, outLength + 2)

	local falsie13 = math.random(1, outLength + 1)
	if falsie13 > falsie12 then
		table.insert(outOrder[1], falsie13 + 1, outLength + 3)
	else
		table.insert(outOrder[1], falsie13, outLength + 3)
	end
	if falsie13 > falsie23 then
		table.insert(outOrder[3], falsie13 + 1, outLength + 3)
	else
		table.insert(outOrder[3], falsie13, outLength + 3)
	end
	shuffle(outOrder)

-- There are outLength + 2 controlled values per seqeunce, and the rest unconrolled.  Pick the indices.
	setup = {
		{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, },
		{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, },
		{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, },
	}
	outTables = {
		{},
		{},
		{},
	}
	for i = 1, 3 do
		for j = 1, (outLength + 2) do
			index = math.random(1, #setup[i])
			outTables[i][j] = setup[i][index]
			table.remove(setup[i], index)
		end
		table.sort(outTables[i])
	end


	input = {}
	output = {}
	
-- The values used in this puzzle.  First 10 (post-shuffle) are reserved for output and falsies.
	numSet = {}
	numSet[1] = math.random(1, 2)
	for i = 2, 50 do
		numSet[i] = numSet[i-1] + math.random(1, 2)
	end
	shuffle(numSet)

	-- Create output
	for i = 1, outLength do
		output[i] = numSet[i]
	end

	
	-- Create input
	for i = 0, 2 do

		local m = 1 -- m tracks progress through controlled values

		for j = 1, 12 do
			if j == outTables[i+1][m] then
				input[13*i+j] = numSet[outOrder[i+1][m]]
				m = m + 1
			else
				input[13*i+j] = numSet[10+7*i+j] --use of 7 here emulates the prior decision to allow start-end overlap
			end
		end
		input[13*i+13] = 0
	end

	return {
		{ STREAM_INPUT, "IN.F", 3, input },
		{ STREAM_OUTPUT, "OUT.G", 1, output },
	}
end

function shuffle(tbl)
	local size = #tbl
	for i = size, 1, -1 do
		local rand = math.random(1, size)
		tbl[i], tbl[rand] = tbl[rand], tbl[i]
	end
	return tbl
end

-- The function get_layout() should return an array of exactly 12 TILE_* values, which
-- describe the layout and type of tiles found in the puzzle.
--
-- TILE_COMPUTE: A basic execution node (node type T21).
-- TILE_MEMORY: A stack memory node (node type T30).
-- TILE_DAMAGED: A damaged execution node, which acts as an obstacle.
--
function get_layout()
	return { 
		TILE_COMPUTE, 	TILE_COMPUTE, 	TILE_COMPUTE, 	TILE_COMPUTE,
		TILE_MEMORY, 	TILE_COMPUTE,	TILE_COMPUTE, 	TILE_COMPUTE,
		TILE_DAMAGED, 	TILE_COMPUTE,	TILE_MEMORY, 	TILE_COMPUTE,
	}
end