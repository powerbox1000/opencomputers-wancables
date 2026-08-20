# WAN Cables for OpenComputers
A WIP mod to add semi-realistic WAN cables that run through unloaded chunks to OpenComputers.

## Lua API Documentation

### Modem (`wan_modem`) Component
The `wan_modem` component is proved by the Modem block. It has an internal buffer which can queue up to five packets for transmission.

#### `signal wan_modem_message(modemAddress: string, message: string)`
This signal is fired when the modem recieves a message from the WAN network, with the address of the recieving modem and the contents of the message.

#### `signal wan_modem_buffer_drained(modemAddress: string)`
This signal is fired when the modem's internal buffer is drained. If a transmission fails due to a full buffer, await this signal first.

#### `function getTransferRate(): number`
Returns the currently configured data transfer rate in blocks/tick.

#### `function getMTU(): number`
Returns the currently configured maximum transmission unit (MTU) in bytes.

#### `function isBufferFull(): boolean`
Returns whether the internal buffer is full.

#### `function send(string data): boolean, string | nil`
Queues the provided message to be transmitted across the connected WAN network. All modems reachable at the time of sending will recieve the message unless the world closes, and all recieving modems are chunk loaded for five seconds to allow processing.
Returns whether the message was successfully queued for transmission. If not successful, the second return value is a string describing the error.
##### Errors
- `not enough power` - Modems draw 5 OC energy units when transmitting. The message will not transmit if the modem cannot draw this power.
- `packet too large` - Modems cannot transmit a packet with an amount of bytes larger than the MTU.
- `internal buffer full` - Modems can only transmit a maximum of bytes equal to the MTU per tick. If this limit is exceeded, the transmission will be queued for a future tick. However, if the buffer exceeds five queued packets, the transmission will fail.
