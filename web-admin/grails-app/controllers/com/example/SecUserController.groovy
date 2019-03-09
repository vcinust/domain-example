package com.example

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Transactional(readOnly = true)
@Secured(['ROLE_ADMIN'])
class SecUserController {

	static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

	def index(Integer max) {
		params.max = Math.min(max ?: 10, 100)
		respond SecUser.list(params), model:[secUserCount: SecUser.count()]
	}

	def show(SecUser secUser) {
		respond secUser
	}

	def create() {
		respond new SecUser(), model: [roleList: SecRole.listOrderByAuthority()]
	}

	@Transactional
	def save(SecUser secUser) {
		if (secUser == null) {
			transactionStatus.setRollbackOnly()
			notFound()
			return
		}

		if (secUser.hasErrors()) {
			transactionStatus.setRollbackOnly()
			respond secUser.errors, view:'create'
			return
		}

		secUser.save flush:true
		this.addRoles(secUser)

		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.created.message', args: [message(code: 'secUser.label', default: 'SecUser'), secUser.id])
				redirect secUser
			}
			'*' { respond secUser, [status: CREATED] }
		}
	}

	def edit(SecUser secUser) {
		respond secUser, model: [roleList: SecRole.listOrderByAuthority()]
	}

	@Transactional
	def update(SecUser secUser) {
		if (secUser == null) {
			transactionStatus.setRollbackOnly()
			notFound()
			return
		}

		if (secUser.hasErrors()) {
			transactionStatus.setRollbackOnly()
			respond secUser.errors, view:'edit'
			return
		}

		if(doUpdate(secUser)){
			request.withFormat {
				form multipartForm {
					flash.message = message(code: 'default.updated.message', args: [
						message(code: 'SecUser.label', default: 'SecUser'),
						secUser.id
					])
					redirect secUser
				}
				'*'{ respond secUser, [status: OK] }
			}
		}
		else {
			respond secUser.errors, view:'edit', model: [roleList: SecRole.listOrderByAuthority()]
		}
	}

	@Transactional
	def delete(SecUser secUser) {

		if (secUser == null) {
			transactionStatus.setRollbackOnly()
			notFound()
			return
		}

		SecUserSecRole.removeAll(secUser)  // remove all roles
		secUser.delete flush:true

		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.deleted.message', args: [message(code: 'secUser.label', default: 'SecUser'), secUser.id])
				redirect action:"index", method:"GET"
			}
			'*'{ render status: NO_CONTENT }
		}
	}

	@Transactional
	private boolean doUpdate(secUser) {
		if (secUser.save(flush: true)) {
			SecUserSecRole.removeAll(secUser)  // remove all roles
			addRoles(secUser)  // add the new roles - which may be the old ones
			return true
		}
		else{
			return false
		}
	}

	private void addRoles(secUser) {
		if(!secUser.isAttached()){
			secUser.attach()
		}
		def roleIds = params.secRole
		if(roleIds instanceof java.lang.String){
			def role = SecRole.get(Integer.parseInt(roleIds))
			if(!role.isAttached()){
				role.attach()
			}
			new SecUserSecRole(secUser: secUser, secRole: role).save(flush: true)
		}
		else{
			roleIds.each{
				def role = SecRole.get(Integer.parseInt(it))
				if(!role.isAttached()){
					role.attach()
				}
				new SecUserSecRole(secUser: secUser, secRole: role).save(flush: true)
			}
		}
	}

	protected void notFound() {
		request.withFormat {
			form multipartForm {
				flash.message = message(code: 'default.not.found.message', args: [message(code: 'secUser.label', default: 'SecUser'), params.id])
				redirect action: "index", method: "GET"
			}
			'*'{ render status: NOT_FOUND }
		}
	}
}
