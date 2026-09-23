local key = KEYS[1]
local num = tonumber(ARGV[1])

local stock = redis.call('GET', key)
if not stock then
    return -2
end

stock = tonumber(stock)
if stock < num then
    return -1
end

return redis.call('DECRBY', key, num)