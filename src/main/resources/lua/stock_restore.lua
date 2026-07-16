-- 原子回补库存：KEYS[1]=库存key, ARGV[1]=回补数量
redis.call('INCRBY', KEYS[1], tonumber(ARGV[1]))
return 1
