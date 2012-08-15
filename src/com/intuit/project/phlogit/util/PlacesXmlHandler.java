package com.intuit.project.phlogit.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to handle Reverse Geocoding using LBS service.
 * 
 * @author pkaushik
 */
public class PlacesXmlHandler extends DefaultHandler
{

	private boolean inLocalityName = false;

	private boolean finished = false;

	private StringBuilder builder;

	private String houseNumber;

	private String address;

	private String state;

	private boolean inAdministrativeAreaName;

	private boolean inAddress;

	private String street;

	private String city;

	private String zip;

	private String stateCode;

	public String getHouseNumber()
	{
		return this.houseNumber;
	}

	public String getAddress()
	{
		return this.address;
	}

	public String getState()
	{
		return this.state;
	}

	public String getStateCode()
	{
		return this.stateCode;
	}

	public String getStreet()
	{
		return this.street;
	}

	public String getCity()
	{
		return this.city;
	}

	public String getZip()
	{
		return this.zip;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		super.characters(ch, start, length);
		if ((!inLocalityName || !inAddress || !inAdministrativeAreaName) || !this.finished)
		{
			if ((ch[start] != '\n') && (ch[start] != ' '))
			{
				builder.append(ch, start, length);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException
	{
		super.endElement(uri, localName, name);

		if (!this.finished)
		{
			if (localName.equalsIgnoreCase("houseNumber"))
			{
				this.houseNumber = builder.toString();
			}

			if (localName.equalsIgnoreCase("street"))
			{
				this.street = builder.toString();
			}

			if (localName.equalsIgnoreCase("city"))
			{
				this.city = builder.toString();
			}

			if (localName.equalsIgnoreCase("administrativeAreaLevel1"))
			{
			}

			if (localName.equalsIgnoreCase("postalCode"))
			{
				this.zip = builder.toString();
			}

			if (localName.equalsIgnoreCase("administrativeAreaLevel1Short"))
			{
				this.stateCode = builder.toString();
			}

			if (builder != null)
			{
				builder.setLength(0);
			}
		}
	}

	@Override
	public void startDocument() throws SAXException
	{
		super.startDocument();
		builder = new StringBuilder();
	}

	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes)
			throws SAXException
	{
		super.startElement(uri, localName, name, attributes);
	}
}
