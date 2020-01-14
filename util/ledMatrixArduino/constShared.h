// do NOT modify this file, use generateSharedConstDef.py to produce this file
#ifndef CONSTSHAREDBYFW_H_
#define CONSTSHAREDBYFW_H_
#define MAX_LED_PAYLOAD_BYTES 260
// 5 bytes: first 3 bytes arbitrary RGB setting(in a rare case it can be M2R). next 2 bytes in real data can never be identical
#define PROTOCOL_PREAMBLE "M2Rbb"
#define PROTOCOL_LENBYTES 2
#endif /* CONSTSHAREDBYFW_H_ */
