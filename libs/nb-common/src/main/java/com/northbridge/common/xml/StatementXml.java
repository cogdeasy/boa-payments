package com.northbridge.common.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * JAXB helpers for the STMT-XML 2.1 interchange format exchanged with the print vendor and the
 * statements service.
 */
public final class StatementXml {

    private StatementXml() {
    }

    public static String marshal(Object document) {
        try {
            JAXBContext ctx = JAXBContext.newInstance(document.getClass());
            Marshaller m = ctx.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter out = new StringWriter();
            m.marshal(document, out);
            return out.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to marshal " + document.getClass().getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(InputStream in, Class<T> type) {
        try {
            Unmarshaller u = JAXBContext.newInstance(type).createUnmarshaller();
            return (T) u.unmarshal(in);
        } catch (JAXBException e) {
            throw new IllegalStateException("Unable to unmarshal " + type.getSimpleName(), e);
        }
    }
}
