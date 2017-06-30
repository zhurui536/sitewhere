import {restAuthGet, restAuthPost, restAuthDelete} from './http-common'

/**
 * List sites.
 */
export function listSites (store, query, success, failed) {
  restAuthGet(store, 'sites?' + query, success, failed)
}

/**
 * Get a site by unique token.
 */
export function getSite (store, siteToken, success, failed) {
  restAuthGet(store, 'sites/' + siteToken, success, failed)
}

/**
 * List assignments for a site.
 */
export function listAssignmentsForSite (store, siteToken, query, success, failed) {
  restAuthGet(store, 'sites/' + siteToken + '/assignments?' + query +
    '&includeDevice=true&includeAsset=true', success, failed)
}

/**
 * List location events for a site.
 */
export function listLocationsForSite (store, siteToken, query, success, failed) {
  restAuthGet(store, 'sites/' + siteToken + '/locations?' + query, success, failed)
}

/**
 * List measurement events for a site.
 */
export function listMeasurementsForSite (store, siteToken, query, success, failed) {
  restAuthGet(store, 'sites/' + siteToken + '/measurements?' + query, success, failed)
}

/**
 * List alert events for a site.
 */
export function listAlertsForSite (store, siteToken, query, success, failed) {
  restAuthGet(store, 'sites/' + siteToken + '/alerts?' + query, success, failed)
}

/**
 * List zones for a site.
 */
export function listZonesForSite (store, siteToken, query, success, failed) {
  restAuthGet(store, 'sites/' + siteToken + '/zones?' + query, success, failed)
}

/**
 * Create zone.
 */
export function createZone (store, siteToken, payload, success, failed) {
  restAuthPost(this.$store, '/sites/' + siteToken + '/zones', payload, success, failed)
}

/**
 * Delete zone.
 */
export function deleteZone (store, zoneToken, success, failed) {
  restAuthDelete(store, 'zones/' + zoneToken + '?force=true', success, failed)
}

/**
 * Release an active assignment.
 */
export function releaseAssignment (store, token, success, failed) {
  restAuthPost(store, '/assignments/' + token + '/end', null, success, failed)
}

/**
 * Mark an assignment as missing.
 */
export function missingAssignment (store, token, success, failed) {
  restAuthPost(store, '/assignments/' + token + '/missing', null, success, failed)
}
