-- KEYS[1] = ai:{memberId}:token-bucket
-- ARGV[1]=capacity, ARGV[2]=refillPerSec, ARGV[3]=cost, ARGV[4]=idleTtlSec
local t = redis.call('TIME')
local nowMs = t[1] * 1000 + math.floor(t[2] / 1000)

local cap = tonumber(ARGV[1])
local refillSec = tonumber(ARGV[2])
local cost = tonumber(ARGV[3])
local ttlSec = tonumber(ARGV[4])

local vals = redis.call('HMGET', KEYS[1], 'tokens', 'ts')
local tokens = tonumber(vals[1])
local ts = tonumber(vals[2])
if not tokens then
    tokens = cap;
    ts = nowMs
end

local elapsedMs = nowMs - ts
if elapsedMs < 0 then
    elapsedMs = 0
end
tokens = math.min(cap, tokens + (elapsedMs / 1000.0) * refillSec)

local allowed = false
local retryAfterMs = 0

if tokens + 1e-9 >= cost then
    allowed = true
    tokens = tokens - cost
else
    if refillSec <= 0 then
        retryAfterMs = 60000
    else
        local need = (cost - tokens)
        retryAfterMs = math.floor((need / refillSec) * 1000)
    end
end

redis.call('HMSET', KEYS[1], 'tokens', tostring(tokens), 'ts', tostring(nowMs))
if ttlSec > 0 then
    redis.call('EXPIRE', KEYS[1], ttlSec)
end

return cjson.encode({ allowed = allowed, retryAfterMs = retryAfterMs, tokensLeft = tokens })
