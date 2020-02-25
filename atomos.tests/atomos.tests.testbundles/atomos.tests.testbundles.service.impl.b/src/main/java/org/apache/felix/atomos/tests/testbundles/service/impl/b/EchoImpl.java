/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.atomos.tests.testbundles.service.impl.b;

import org.apache.felix.atomos.tests.testbundles.service.contract.Echo;
import org.osgi.service.component.annotations.Component;

@Component(property = { "type=impl.b.component" })
public class EchoImpl implements Echo
{

    @Override
    public String echo(String msg)
    {
        return "impl.b.component " + msg;
    }

}