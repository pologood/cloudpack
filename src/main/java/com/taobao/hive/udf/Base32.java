package com.taobao.hive.udf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * ����Base32����
 * @author huangshang
 */
@SuppressWarnings("unchecked")
public class Base32 {
    private static final Map    ENCODE_MAP   = new HashMap();
    private static final Map    DECODE_MAP   = new HashMap();
    private static final String base32Chars  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[]  base32Lookup = {
            0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, // '0', '1', '2', '3', '4', '5', '6', '7'
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // '8', '9', ':', ';', '<', '=', '>', '?'
            0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '@', 'A', 'B', 'C', 'D', 'E', 'F', 'G'
            0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O'
            0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W'
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 'X', 'Y', 'Z', '[', '\', ']', '^', '_'
            0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '`', 'a', 'b', 'c', 'd', 'e', 'f', 'g'
            0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o'
            0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'p', 'q', 'r', 's', 't', 'u', 'v', 'w'
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF,
            0xFF // 'x', 'y', 'z', '{', '|', '}', '~', 'DEL'
        };

    static {
        ENCODE_MAP.put("G", "GBK");
        ENCODE_MAP.put("U", "UTF-8");

        for (Iterator it = ENCODE_MAP.entrySet().iterator(); it.hasNext();) {
            Map.Entry e = (Map.Entry) it.next();

            DECODE_MAP.put(e.getValue(), e.getKey());
        }
    }

    /**
     * ���б���
     * @param bytes
     * @return
     */
    public static final String encode(final byte[] bytes) {
        int          i        = 0;
        int          index    = 0;
        int          digit    = 0;
        int          currByte;
        int          nextByte;
        StringBuffer base32   = new StringBuffer(((bytes.length + 7) * 8) / 5);

        while (i < bytes.length) {
            currByte = (bytes[i] >= 0) ? bytes[i]
                                       : (bytes[i] + 256); // unsign

            /* Is the current digit going to span a byte boundary? */
            if (index > 3) {
                if ((i + 1) < bytes.length) {
                    nextByte = (bytes[i + 1] >= 0) ? bytes[i + 1]
                                                   : (bytes[i + 1] + 256);
                } else {
                    nextByte = 0;
                }

                digit     = currByte & (0xFF >> index);
                index     = (index + 5) % 8;
                digit <<= index;
                digit |= (nextByte >> (8 - index));
                i++;
            } else {
                digit     = (currByte >> (8 - (index + 5))) & 0x1F;
                index     = (index + 5) % 8;

                if (index == 0) {
                    i++;
                }
            }

            base32.append(base32Chars.charAt(digit));
        }

        return base32.toString();
    }

    /**
     * ���н���
     * @param base32
     * @return
     */
    public static final byte[] decode(final String base32) {
        int    i;
        int    index;
        int    lookup;
        int    offset;
        int    digit;
        byte[] bytes = new byte[(base32.length() * 5) / 8];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - '0';

            /* Skip chars outside the lookup table */
            if ((lookup < 0) || (lookup >= base32Lookup.length)) {
                continue;
            }

            digit = base32Lookup[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit == 0xFF) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;

                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;

                    if (offset >= bytes.length) {
                        break;
                    }
                } else {
                    bytes[offset] |= (digit << (8 - index));
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }

                bytes[offset] |= (digit << (8 - index));
            }
        }

        return bytes;
    }

    /**
     * ��һ���ַ������б���
     * @param str
     * @return
     */
    public static final String encodeString(String str) {
        if (null == str) {
            return null;
        }

        try {
            StringBuffer sb     = new StringBuffer();
            String       encode = "GBK"; //��ʹ��GBK��Ĭ��ֵ
            byte[]       bs     = str.getBytes(encode);

            sb.append(DECODE_MAP.get(encode)).append(",");
            sb.append(encode(bs));
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * ��һ���ַ������н���
     * @param str
     * @return
     */
    public static final String decodeString(String str) {
        if ((null == str) || (str.length() < 3)) {
            return str;
        }

        str = str.toUpperCase();
        try {
            String encodeKey = str.substring(0, 1);
            String encode = (String) ENCODE_MAP.get(encodeKey);

            if (null == encode) {
                encode = "GBK";
            }

            byte[] bs = decode(str.substring(2));

            return new String(bs, encode);
        } catch (Exception e) {
            return "";
        }
    }
}