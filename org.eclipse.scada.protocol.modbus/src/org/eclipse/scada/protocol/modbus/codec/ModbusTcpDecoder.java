/*******************************************************************************
 * Copyright (c) 2013 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.scada.protocol.modbus.codec;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.eclipse.scada.protocol.modbus.Constants;
import org.eclipse.scada.protocol.modbus.io.ModbusProtocolError;
import org.eclipse.scada.protocol.modbus.message.Pdu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModbusTcpDecoder extends CumulativeProtocolDecoder
{
    private static final Logger logger = LoggerFactory.getLogger ( ModbusTcpDecoder.class );

    public ModbusTcpDecoder ()
    {
        super ();
    }

    @Override
    protected boolean doDecode ( IoSession session, IoBuffer in, ProtocolDecoderOutput out ) throws Exception
    {
        while ( in.remaining () >= Constants.TCP_HEADER_SIZE )
        {
            // peek body size
            final short bodySize = in.getShort ( in.position () + 2 + 2 );

            if ( in.remaining () < Constants.TCP_HEADER_SIZE + bodySize - 1 ) // unit identifier is part of length, so it has to be subtracted
            {
                // message is not complete so skip for next try
                return false;
            }
            logger.trace ( "doDecode () frame = {}", in.getHexDump () );

            final int transactionIdentifier = in.getUnsignedShort ();
            logger.trace ( "transaction identifier: {}", transactionIdentifier );

            // ensure specification compliance
            final short protocolIdentifier = in.getShort ();
            if ( protocolIdentifier != 0 )
            {
                throw new ModbusProtocolError ( String.format ( "protocol identifier must be 0, but was: %d", protocolIdentifier ) );
            }

            // remove length
            in.getShort ();
            
            // get unit id
            final byte unitIdentifier = in.get ();

            final IoBuffer pdu = IoBuffer.allocate ( in.limit () - Constants.TCP_HEADER_SIZE );
            for ( int i = 0; i < in.limit () - Constants.TCP_HEADER_SIZE; i++ )
            {
                pdu.put ( in.get () );
            }
            pdu.flip ();

            out.write ( new Pdu ( transactionIdentifier, unitIdentifier, pdu ) );
        }
        return false;
    }
}
