package org.eclipse.scada.protocol.iec60870.asdu.message;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scada.protocol.iec60870.ProtocolOptions;
import org.eclipse.scada.protocol.iec60870.asdu.ASDUHeader;
import org.eclipse.scada.protocol.iec60870.asdu.types.ASDU;
import org.eclipse.scada.protocol.iec60870.asdu.types.InformationEntry;
import org.eclipse.scada.protocol.iec60870.asdu.types.InformationObjectAddress;
import org.eclipse.scada.protocol.iec60870.asdu.types.InformationStructure;
import org.eclipse.scada.protocol.iec60870.asdu.types.TypeHelper;
import org.eclipse.scada.protocol.iec60870.asdu.types.Value;


@ASDU ( id = 48, name = "C_SE_NA_1", informationStructure = InformationStructure.SINGLE )
public class SetPointCommandNormalizedValue extends AbstractMeasuredValueFloatingPoint
{
	private final static double M_FORMAT = 1 << 15; // 1.M = 1.15 (доп.код)
	
    private SetPointCommandNormalizedValue ( final ASDUHeader header, final List<InformationEntry<Float>> entries )
    {
        super ( header, entries, false );
    }

    public static SetPointCommandNormalizedValue parse ( final ProtocolOptions options, final byte length, final ASDUHeader header, final ByteBuf data )
    {
        return new SetPointCommandNormalizedValue ( header, parseNormalizedEntries ( options, length, data, false ) );
    }

    public static SetPointCommandNormalizedValue create ( final ASDUHeader header, final InformationObjectAddress address, final Value<Float> value )
    {
        return createInternal ( header, Collections.singletonList ( new InformationEntry<> ( address, value ) ) );
    }

    private static SetPointCommandNormalizedValue createInternal ( final ASDUHeader header, final List<InformationEntry<Float>> values )
    {
        return new SetPointCommandNormalizedValue ( header, values );
    }
    
    @Override
    public void encode ( final ProtocolOptions options, final ByteBuf out )
    {
        EncodeHelper.encodeHeader ( this, options, this.entries.size (), this.header, out );
        
        if (this.entries.size() != 1) {
        	throw new UnsupportedOperationException("Few values provided");
        }
        
        
        for ( final InformationEntry<Float> entry : this.entries )
        {
            entry.getAddress ().encode ( options, out );
            
            this.encodeScaledFloatValue(out, entry.getValue ());
        }
    }
    
    /**
     * Encode Short scaled floating point number with quality descriptor
     */
    private void encodeScaledFloatValue ( final ByteBuf out, final Value<Float> value)
    {
    	Double scaled = value.getValue() * M_FORMAT;
    	short valuable = scaled.shortValue();
    	boolean ov = false;
    	
    	if (scaled >= M_FORMAT || scaled < -M_FORMAT) {
    		ov = true;
    	}
    	
        final byte qds = (byte) ( ov ? 0b00000001 : 0b00000000 );
        final byte siq = value.getQualityInformation ().apply ( qds );
    	
    	out.writeShort(valuable);
        out.writeByte (siq);
    }
    
    protected static List<InformationEntry<Float>> parseNormalizedEntries ( final ProtocolOptions options, final byte length, final ByteBuf data, final boolean withTimestamp )
    {
        final List<InformationEntry<Float>> values = new ArrayList<> ( length );
        for ( int i = 0; i < length; i++ )
        {
            final InformationObjectAddress address = InformationObjectAddress.parse ( options, data );
            final Value<Short> value = TypeHelper.parseScaledValue ( options, data, false );
            
            final Value<Float> floatValue = new Value<Float>( ((float)(value.getValue() / SetPointCommandNormalizedValue.M_FORMAT)), value.getTimestamp(), value.getQualityInformation());
            values.add ( new InformationEntry<> ( address, floatValue ) );
        }
        return values;
    }
}