-- 原子扣减库存：KEYS[1]=库存key, ARGV[1]=扣减数量, ARGV[2]=商品状态key
-- 返回值: 1=扣减成功, 0=库存不足, -1=商品不可售
local stock = tonumber(redis.call('GET', KEYS[1]))
local status = redis.call('GET', KEYS[2])
if status == false or tonumber(status) ~= 1 then
    return -1
end
if stock == false or tonumber(stock) < tonumber(ARGV[1]) then
    return 0
end
redis.call('DECRBY', KEYS[1], tonumber(ARGV[1]))
return 1
