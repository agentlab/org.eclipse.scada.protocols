/*******************************************************************************
 * Copyright (c) 2014 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.scada.protocol.iec60870.asdu.message;

import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.List;

import org.eclipse.scada.protocol.iec60870.ProtocolOptions;
import org.eclipse.scada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.scada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.scada.protocol.iec60870.asdu.types.Cause;
import org.eclipse.scada.protocol.iec60870.asdu.types.InformationEntry;
import org.eclipse.scada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.eclipse.scada.protocol.iec60870.asdu.types.InformationStructure;
import org.eclipse.scada.protocol.iec60870.asdu.types.QualityInformation;
import org.eclipse.scada.protocol.iec60870.asdu.types.TypeHelper;
import org.eclipse.scada.protocol.iec60870.asdu.types.Value;


@ASDU ( id = 58, name = "C_SC_TA_1", informationStructure = InformationStructure.SINGLE )
public class SingleCommandCP56Time2a extends AbstractInformationObjectMessage implements MirrorableMessage<SingleCommandCP56Time2a>
{
    private final Value<Boolean> state;

    private final byte type;

    private final boolean execute;

    public SingleCommandCP56Time2a ( final ASDUHeader header, final InformationObjectAddress informationObjectAddress, final Value<Boolean> state, final byte type, final boolean execute )
    {
        super ( header, informationObjectAddress );
        this.state = state;
        this.type = type;
        this.execute = execute;
    }

    public SingleCommandCP56Time2a ( final ASDUHeader header, final InformationObjectAddress informationObjectAddress, final Value<Boolean> state )
    {
        this ( header, informationObjectAddress, state, (byte)0, true );
    }

    public byte getType ()
    {
        return this.type;
    }

    public boolean isState ()
    {
        return this.state.getValue();
    }

    public Value<Boolean> getState ()
    {
        return this.state;
    }

    public boolean isExecute ()
    {
        return this.execute;
    }
    
    public List<InformationEntry<Boolean>> getEntries() {
    	return Collections.singletonList(new InformationEntry<Boolean>(this.getInformationObjectAddress(), this.state));
    }

    @Override
    public SingleCommandCP56Time2a mirror ( final Cause cause, final boolean positive )
    {
        return new SingleCommandCP56Time2a ( this.header.clone ( cause, positive ), this.informationObjectAddress, this.state, this.type, this.execute );
    }

    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, null, this.header, out );
        byte b = 0;

        b |= this.state.getValue() ? 0b00000001 : 0;
        b |= this.type << 2 & 0b011111100;
        b |= this.execute ? 0 : 0b100000000;

        this.informationObjectAddress.encode ( options, out );
        
        out.writeByte ( b );
        
        TypeHelper.encodeTimestamp(options, out, this.state.getTimestamp());
    }


    public static SingleCommandCP56Time2a parse ( final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        final InformationObjectAddress address = InformationObjectAddress.parse ( options, data );

        final byte b = data.readByte ();

        final Value<Boolean> state = new Value<Boolean> ( ( b & 0b00000001 ) > 0, TypeHelper.parseTimestamp(options, data), QualityInformation.OK ) ; // parse timestamp, quality
        final byte type = (byte) ( ( b & 0b011111100 ) >> 2 );
        final boolean execute = ! ( ( b & 0b100000000 ) > 0 );
        

        return new SingleCommandCP56Time2a ( header, address, state, type, execute );
    }

}
