package org.jivesoftware.openfire.plugin.xauth;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.user.UserProvider;

public class XUserProvider implements UserProvider {

	@Override
	public User loadUser(String username) throws UserNotFoundException {
		User user = new User(username,username,"yanricheng@163.com",new Date(),new Date());
		return user;
	}

	@Override
	public User createUser(String username, String password, String name,
			String email) throws UserAlreadyExistsException {
		return null;
	}

	@Override
	public void deleteUser(String username) {

	}

	@Override
	public int getUserCount() {
		return 0;
	}

	@Override
	public Collection<User> getUsers() {
		return CollectionUtils.EMPTY_COLLECTION;
	}

	@Override
	public Collection<String> getUsernames() {
		return CollectionUtils.EMPTY_COLLECTION;
	}

	@Override
	public Collection<User> getUsers(int startIndex, int numResults) {
		return CollectionUtils.EMPTY_COLLECTION;
	}

	@Override
	public void setName(String username, String name)
			throws UserNotFoundException {
	}

	@Override
	public void setEmail(String username, String email)
			throws UserNotFoundException {

	}

	@Override
	public void setCreationDate(String username, Date creationDate)
			throws UserNotFoundException {

	}

	@Override
	public void setModificationDate(String username, Date modificationDate)
			throws UserNotFoundException {

	}

	@Override
	public Set<String> getSearchFields() throws UnsupportedOperationException {
		return null;
	}

	@Override
	public Collection<User> findUsers(Set<String> fields, String query)
			throws UnsupportedOperationException {
		return null;
	}

	@Override
	public Collection<User> findUsers(Set<String> fields, String query,
			int startIndex, int numResults)
			throws UnsupportedOperationException {
		return null;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public boolean isNameRequired() {
		return false;
	}

	@Override
	public boolean isEmailRequired() {
		return false;
	}

}
