-- The function get_name() should return a single string that is the name of the puzzle.
--
function get_name()
	return "NUMERAL AMALGAMATOR"
end

-- The function get_description() should return an array of strings, where each string is
-- a line of description for the puzzle. The text you return from get_description() will 
-- be automatically formatted and wrapped to fit inside the puzzle information box.
--
function get_description()
	return { "IN.DI IS A SEQUENCE OF POSITIONS", "IN.NU IS A SEQUENCE OF NUMBERS", "  EXTRACT FROM IN.NU THE DIGIT", "  SPECIFIED BY IN.DI", "  REPEAT THREE TIMES AND", "  CONCATENATE THOSE NUMERALS" }
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
	my0 = math.random(1,13)
	my999 = math.random(1,12)
	if my999 >= my0 then my999 = my999 + 1 end

	inputA = {}
	inputB = {}
	output = {}
	for i = 1,13 do
		if i == my0 then
			for j = 0,2 do
				inputA[(3*i)-j] =  math.random(2, 3)
				inputB[(3*i)-j] = math.floor(math.sqrt(10*math.random(10,640)))
				if inputA[(3*i)-j] == 3 then
					inputB[(3*i)-j] = inputB[(3*i)-j] * 10
				else
					inputB[(3*i)-j] = (math.floor(inputB[(3*i)-j] / 10) * 100 + (inputB[(3*i)-j] % 10))
				end
			end
			output[i] = 0
		elseif i == my999 then
			for j = 0,2 do
				inputA[(3*i)-j] =  math.random(2, 3)
				inputB[(3*i)-j] = math.floor(math.sqrt(10*math.random(10,640)))
				if inputA[(3*i)-j] == 3 then
					inputB[(3*i)-j] = inputB[(3*i)-j] * 10 + 9
				else
					inputB[(3*i)-j] = (math.floor(inputB[(3*i)-j] / 10) * 100 + (inputB[(3*i)-j] % 10) + 90)
				end
			end
			output[i] = 999
		else
			inputA[(3*i)-2] = math.random(1, 3)
			inputA[(3*i)-1] = math.random(1, 3)
			inputA[(3*i)] = math.random(1, 3)
			inputB[(3*i)-2] = math.floor(math.sqrt(1000*math.random(10,640)))
			inputB[(3*i)-1] = math.floor(math.sqrt(1000*math.random(10,640)))
			inputB[(3*i)] = math.floor(math.sqrt(1000*math.random(10,640)))
			output[i] = 100*math.floor((inputB[(3*i)-2]/math.pow(10, 3-inputA[(3*i)-2]))%10)
			output[i] = output[i] + 10*math.floor((inputB[(3*i)-1]/math.pow(10, 3-inputA[(3*i)-1]))%10)
			output[i] = output[i] + math.floor((inputB[(3*i)]/math.pow(10, 3-inputA[(3*i)]))%10)
		end
	end
	return {
		{ STREAM_INPUT, "IN.DI", 1, inputA },
		{ STREAM_INPUT, "IN.NU", 2, inputB },
		{ STREAM_OUTPUT, "OUT.AM", 2, output },
	}
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
		TILE_COMPUTE, 	TILE_COMPUTE, 	TILE_COMPUTE, 	TILE_DAMAGED,
		TILE_COMPUTE, 	TILE_COMPUTE,	TILE_COMPUTE, 	TILE_COMPUTE,
		TILE_MEMORY, 	TILE_COMPUTE,	TILE_COMPUTE, 	TILE_COMPUTE,
	}
end