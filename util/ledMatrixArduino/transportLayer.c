

#include <Arduino.h> // uint8_t is defined here
#include "transportLayer.h"

ParserStatus m_sParserStatus = {
    .ParserState = ProtocolPreamble,
    .ByteExpected = 0,
    .ByteFilled = 0};
    
uint8_t m_pOneMsgBuf[MAX_LED_PAYLOAD_BYTES];
bool bEntryEvent = true;

void transition2newState(ProtocolParserState newState)
{
    m_sParserStatus.ParserState = newState;
    bEntryEvent = true;
}

VarLenProtocolParserResult onProtocolByte(uint8_t c)
{
    switch (m_sParserStatus.ParserState)
    {
        case ProtocolPreamble:
        {
            if (bEntryEvent)
            {
                bEntryEvent = false;
                // m_sParserStatus._computedCRC = 0; CRC is not computed for Preamble, the entry event of PayloadByteCnt16bit resets CRC status
                m_sParserStatus.ByteFilled = 0;
            }
            if(c == PROTOCOL_PREAMBLE[m_sParserStatus.ByteFilled])
            {
                m_sParserStatus.ByteFilled++;
                if(m_sParserStatus.ByteFilled == PROTOCOL_PREAMBLE_BYTES)
                {
                    // matched
                    m_sParserStatus.ByteFilled = 0;
                    transition2newState(PayloadByteCnt16bit);
                }
            }
            else
            {
                // TODO: In case preamble has repeated pattern such as ASDDASDD
                m_sParserStatus.ByteFilled = 0;
            }       
            return SVTELEMPARSER_INCOMPLETE;
        }
        break;
        case PayloadByteCnt16bit:
        {
            if (bEntryEvent)
            {
                bEntryEvent = false;
                m_sParserStatus.ByteFilled = 0;
            }
            m_pOneMsgBuf[m_sParserStatus.ByteFilled] = c;
            if (m_sParserStatus.ByteFilled == 0)
            {
                m_sParserStatus.ByteFilled++;
            }
            else if (m_sParserStatus.ByteFilled == 1)
            {
                // byte order: little endian
                // m_sParserStatus.ByteExpected = m_pOneMsgBuf[0] + ((uint16_t)m_pOneMsgBuf[1])<<8; this gives wrong answer due to byte alignment
                m_sParserStatus.ByteExpected = m_pOneMsgBuf[0] + m_pOneMsgBuf[1]*256;
                if (m_sParserStatus.ByteExpected > MAX_LED_PAYLOAD_BYTES )
                {
                    transition2newState(ProtocolPreamble);
                    return SVTELEMPARSER_PAYLOADLENVIOLATION;
                }
                transition2newState(ProtocolBody);
            }
            return SVTELEMPARSER_INCOMPLETE;
        }
        break;
        case ProtocolBody:
        {
            if (bEntryEvent)
            {
                bEntryEvent = false;
                m_sParserStatus.ByteFilled = 0;
            }
            m_pOneMsgBuf[m_sParserStatus.ByteFilled] = c;
            m_sParserStatus.ByteFilled ++;
            if (m_sParserStatus.ByteFilled == m_sParserStatus.ByteExpected)
            {
                transition2newState(ProtocolPreamble);
                return SVTELEMPARSER_COMPLETE;
            }
            return SVTELEMPARSER_INCOMPLETE;
        }
        break;
        default:
            return SVTELEMPARSER_ERROR;
            break;
    }
}
