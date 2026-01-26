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
			-- force this output to be 0
			for j = 0,2 do
				p = math.random(2, 3)
				d = decompose(randomInput())
				inputA[3*i-j] = p
				if p == 3 then
					inputB[3*i-j] = amalgamate(d[1], d[2], 0)
				else
					inputB[3*i-j] = amalgamate(d[1], 0, d[2])
				end
			end
			output[i] = 0
		elseif i == my999 then
			-- force this output to be 999
			for j = 0,2 do
				p = math.random(2, 3)
				d = decompose(randomInput())
				inputA[3*i-j] = p
				-- avoid input going above 800
				if d[1] == 8 then d[1] = 7 end
				if p == 3 then
					inputB[3*i-j] = amalgamate(d[1], d[2], 9)
				else
					inputB[3*i-j] = amalgamate(d[1], 9, d[2])
				end
			end
			output[i] = 999
		else
			p1 = math.random(1, 3)
			p2 = math.random(1, 3)
			p3 = math.random(1, 3)
			v1 = randomInput()
			v2 = randomInput()
			v3 = randomInput()

			inputA[3*i-2] = p1
			inputA[3*i-1] = p2
			inputA[3*i] = p3
			inputB[3*i-2] = v1
			inputB[3*i-1] = v2
			inputB[3*i] = v3
			output[i] = amalgamate(decompose(v1)[p1], decompose(v2)[p2], decompose(v3)[p3])
		end
	end

	return {
		{ STREAM_INPUT, "IN.DI", 1, inputA },
		{ STREAM_INPUT, "IN.NU", 2, inputB },
		{ STREAM_OUTPUT, "OUT.AM", 2, output },
	}
end

function randomInput()
	-- generate random value between 100 and 800, biased toward bigger numbers
	return math.floor(math.sqrt(1000*math.random(10,640)))
end

function amalgamate(d1, d2, d3)
	return 100*d1+10*d2+d3
end

function decompose(v)
	return {
		(math.floor(v/100)) % 10,
		(math.floor(v/10)) % 10,
		(math.floor(v)) % 10,
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
