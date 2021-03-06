/*-------------------------------------------------------------------------------------------------------------------*\
|  Copyright (C) 2014 eBay Software Foundation                                                                        |
|                                                                                                                     |
|  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance     |
|  with the License.                                                                                                  |
|                                                                                                                     |
|  You may obtain a copy of the License at                                                                            |
|                                                                                                                     |
|       http://www.apache.org/licenses/LICENSE-2.0                                                                    |
|                                                                                                                     |
|  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed   |
|  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for  |
|  the specific language governing permissions and limitations under the License.                                     |
\*-------------------------------------------------------------------------------------------------------------------*/

package com.paypal.selion.plugins;

import java.util.List;

import com.paypal.selion.elements.IOSSeLionElement;
import com.paypal.selion.elements.HtmlSeLionElement;

/**
 * A simple POJO class that represents information pertaining to a html object.
 * 
 */
public class GUIObjectDetails {

    private String memberType;
    private String memberName;
    private String parent;

    public GUIObjectDetails(String memberType, String memberName) {
        this(memberType, memberName, null);
    }

    public GUIObjectDetails(String memberType, String memberName, String parent) {
        this.memberType = memberType;
        this.memberName = memberName;
        this.parent = parent;
    }

    public String getMemberType() {
        return memberType;
    }


    public String getMemberName() {
        return memberName;
    }

    public String getParent() {
        return parent;
    }
    
    //This method is used by the velocity template and has reference in Class.vm
    //DONOT tamper with this method
    public String returnArg(String key) {
        HtmlSeLionElement element = HtmlSeLionElement.findMatch(key);
        if (element == null) {
            return key;
        }
        if (! element.isUIElement()) {
            return key;
        }
        return key.substring(0,key.indexOf(element.stringify()));
    }

    //This method is used by the velocity template and has reference in Class.vm
    //DONOT tamper with this method
    public String firstToUpperCase(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    /**
     * This method convert each key in the data sheet into corresponding HtmlObjectDetails object and returns list of
     * HtmlObjectDetails
     * 
     * @param keys
     *            - keys for which {@link GUIObjectDetails} is to be created.
     * @return - list of HtmlObjectDetails
     */
    public static List<GUIObjectDetails> transformKeys(List<String> keys,TestPlatform platform) {
        List<GUIObjectDetails> htmlObjectDetailsList = null;
        
        // Get the HTML object list based on the platform.
        // Note: This part is reached only when there is a valid platform specified.So its safe to proceed without a
        // default case in switch
        switch (platform) {
        case WEB: {
            htmlObjectDetailsList = HtmlSeLionElement.getGUIObjectList(keys);
            break;
        }

        case IOS: {
            htmlObjectDetailsList = IOSSeLionElement.getGUIObjectList(keys);
            break;
        }
        }
        return htmlObjectDetailsList;
    }
    

    /**
     * A overloaded version of transformKeys method which internally specifies {@link TestPlatform.WEB} as the {@link TestPlatform}
     * @param keys
     * 			- keys for which {@link GUIObjectDetails} is to be created.
     * @return
     */
    public static List<GUIObjectDetails> transformKeys(List<String> keys){
    	return transformKeys(keys,TestPlatform.WEB);
    	
    }
    
    /**
     * Method to validate the keys against the {@link HtmlSeLionElement} or {@link IOSSeLionElement} as per the
     * {@link TestPlatform}
     * 
     * @param keysToValidate
     *            - the keys from the Page Yaml input
     * @param dataFileName
     *            - The file name containing the keys
     * @param currentPlatform
     *            - the platform specified in the Page Yaml input
     */
    public static void validateKeysInDataFile(List<String> keysToValidate, String dataFileName,
            TestPlatform currentPlatform) {
        String DELIMITER = "#";
        for (String currentKey : keysToValidate) {

            // For case: Invalid element inside a container , the key inside a container is split using delimiter.
            // It will be assigned to the currentKey to proceed with the validation.
            if (currentKey.contains(DELIMITER)) {
                String[] keyInContainer = currentKey.split(DELIMITER);
                // assigning the key to the current key to proceed with the validation
                currentKey = keyInContainer[1];
            }

            // Validations for WEB
            if ((currentPlatform == TestPlatform.WEB)) {
                /*
                 * For Yaml V1 the non-html elements are added to the List of keys(EG: pageTitle) whereas for V2 it does
                 * not. As a result, if a user specifies wrong name for pageTitle we first check it to be valid name and
                 * then proceed with the usual check of validating if its a html element TODO: This is a hack to be
                 * removed when V1 is fully deprecated
                 */
                if (!(HtmlSeLionElement.isValid(currentKey))) {
                    throw new IllegalArgumentException(String.format(
                            "Detected an invalid key [%s] in data file %s for Platform: WEB", currentKey, dataFileName));
                }

                if (currentKey.equals(HtmlSeLionElement.PAGE_TITLE.stringify())) {
                    continue;
                }

                if (!(HtmlSeLionElement.isValidHtmlElement(currentKey))) {
                    throw new IllegalArgumentException(String.format(
                            "Detected an invalid key [%s] in data file %s for Platform: WEB", currentKey, dataFileName));
                }
            }

            // Validations for IOS
            if ((currentPlatform == TestPlatform.IOS)) {
                if (!(IOSSeLionElement.isValidUIElement(currentKey))) {
                    throw new IllegalArgumentException(String.format(
                            "Detected an invalid key [%s] in data file %s for Platform IOS", currentKey, dataFileName));
                }
            }
        }
    }
}
