/*
 * ******************************************************************************
 *   Copyright (c) 2013-2014 Nicolas POMEPUY.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  *****************************************************************************
 */
package fr.nicolaspomepuy.discreetapprate;

/**
 * Created by nicolas on 06/03/14.
 */
public enum  RetryPolicy {
    /**
     * Will retry each time initial count has been triggered
     * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 9th, ... times
     */
    INCREMENTAL,
    /**
     * Will retry exponentially to be less intrusive
     * Ex: if initial is set to 3, it will be shown on the 3rd, 6th, 12th, ... times
     */
    EXPONENTIAL,
    /**
     * Will never retry
     */
    NONE;
}
